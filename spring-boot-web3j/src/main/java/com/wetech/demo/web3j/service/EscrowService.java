package com.wetech.demo.web3j.service;

import com.wetech.demo.web3j.contracts.escrow.Escrow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscrowService {

    private static final String[] STATUS = {"Created", "Paid", "Completed", "Refunded", "Disputed"};

    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;

    private Escrow contract;
    @Getter private String contractAddress;
    @Getter private BigInteger deployBlock;

    /** 后端部署 Escrow，仲裁人就是后端账户自己 */
    public CompletableFuture<Escrow> deploy() {
        log.info("Deploying Escrow, arbiter={}", credentials.getAddress());
        return Escrow.deploy(web3j, credentials, gasProvider, credentials.getAddress())
                .sendAsync()
                .thenApply(c -> {
                    this.contract = c;
                    this.contractAddress = c.getContractAddress();
                    this.deployBlock = c.getTransactionReceipt()
                            .map(TransactionReceipt::getBlockNumber)
                            .orElse(BigInteger.ZERO);
                    log.info("Escrow deployed at {} (block {})", contractAddress, deployBlock);
                    return c;
                });
    }

    /** 服务重启后加载已有合约；fromBlock 必须是部署区块，否则事件会漏 */
    public void load(String address, BigInteger fromBlock) {
        this.contract = Escrow.load(address, web3j, credentials, gasProvider);
        this.contractAddress = address;
        this.deployBlock = fromBlock;
        log.info("Escrow loaded at {} fromBlock={}", address, fromBlock);
    }

    public CompletableFuture<String> getArbiter() {
        requireContract();
        return contract.arbiter().sendAsync();
    }

    public CompletableFuture<Map<String, String>> getOrder(BigInteger id) {
        requireContract();
        return contract.getOrder(id).sendAsync().thenApply(o -> {
            Map<String, String> order = new LinkedHashMap<>();
            order.put("buyer", o.buyer);
            order.put("seller", o.seller);
            order.put("token", o.token);
            order.put("amount", o.amount.toString());
            order.put("deadline", o.deadline.toString());
            order.put("status", STATUS[o.status.intValue()]);
            order.put("description", o.description);
            return order;
        });
    }

    /**
     * 从 OrderCreated 事件日志重建订单列表。
     * POTOS 的 eth_getLogs 一次只扫 fromBlock 起的 10 个区块，
     * 超出部分静默丢弃，所以必须按 10 个区块一段分批查。
     */
    public List<Map<String, String>> listOrders() throws Exception {
        requireContract();
        BigInteger latest = web3j.ethBlockNumber().send().getBlockNumber();
        BigInteger step = BigInteger.TEN;
        List<Log> logs = new ArrayList<>();

        for (BigInteger from = deployBlock; from.compareTo(latest) <= 0; from = from.add(step)) {
            BigInteger to = from.add(step).subtract(BigInteger.ONE).min(latest);
            EthFilter filter = new EthFilter(
                    DefaultBlockParameter.valueOf(from),
                    DefaultBlockParameter.valueOf(to),
                    contractAddress);
            filter.addSingleTopic(EventEncoder.encode(Escrow.ORDERCREATED_EVENT));
            web3j.ethGetLogs(filter).send().getLogs().forEach(r -> logs.add((Log) r.get()));
        }

        List<Map<String, String>> orders = new ArrayList<>();
        for (Log l : logs) {
            Escrow.OrderCreatedEventResponse e = Escrow.getOrderCreatedEventFromLog(l);
            Map<String, String> order = new LinkedHashMap<>();
            order.put("id", e.id.toString());
            order.put("buyer", e.buyer);
            order.put("seller", e.seller);
            order.put("amount", e.amount.toString());
            order.put("blockNumber", l.getBlockNumber().toString());
            order.put("transactionHash", l.getTransactionHash());
            orders.add(order);
        }
        return orders;
    }

    /** 仲裁：只有 Disputed 状态能成功，否则交易 revert */
    public CompletableFuture<TransactionReceipt> resolve(BigInteger id, boolean sellerWins) {
        requireContract();
        log.info("Resolving order {} sellerWins={}", id, sellerWins);
        return contract.resolve(id, sellerWins).sendAsync();
    }

    private void requireContract() {
        if (contract == null) {
            throw new IllegalStateException("Contract not deployed or loaded, call /deploy or /load first");
        }
    }
}
