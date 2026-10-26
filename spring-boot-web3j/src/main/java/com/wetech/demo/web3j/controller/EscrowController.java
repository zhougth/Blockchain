package com.wetech.demo.web3j.controller;

import com.wetech.demo.web3j.service.EscrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/escrow")
@RequiredArgsConstructor
public class EscrowController {

    private final EscrowService escrowService;

    /** 后端部署 Escrow，返回合约地址、部署交易哈希和部署区块 */
    @PostMapping("/deploy")
    public CompletableFuture<ResponseEntity<Map<String, String>>> deploy() {
        return escrowService.deploy().thenApply(contract -> {
            TransactionReceipt receipt = contract.getTransactionReceipt()
                    .orElseThrow(() -> new IllegalStateException("deploy receipt not found"));
            Map<String, String> body = new LinkedHashMap<>();
            body.put("contractAddress", contract.getContractAddress());
            body.put("transactionHash", receipt.getTransactionHash());
            body.put("deployBlock", receipt.getBlockNumber().toString());
            return ResponseEntity.ok(body);
        });
    }

    /** 服务重启后，用已有地址和部署区块恢复 */
    @PostMapping("/load")
    public ResponseEntity<Map<String, String>> load(@RequestParam String address,
                                                    @RequestParam BigInteger fromBlock) {
        escrowService.load(address, fromBlock);
        Map<String, String> body = new LinkedHashMap<>();
        body.put("contractAddress", address);
        return ResponseEntity.ok(body);
    }

    /** 当前加载的合约地址和仲裁人地址 */
    @GetMapping("/address")
    public CompletableFuture<ResponseEntity<Map<String, String>>> address() {
        return escrowService.getArbiter().thenApply(arbiter -> {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("contractAddress", escrowService.getContractAddress());
            body.put("arbiter", arbiter);
            return ResponseEntity.ok(body);
        });
    }

    /** 从 OrderCreated 事件重建订单列表 */
    @GetMapping("/orders")
    public List<Map<String, String>> listOrders() throws Exception {
        return escrowService.listOrders();
    }

    /** 单笔订单详情，status 返回文字 */
    @GetMapping("/orders/{id}")
    public CompletableFuture<ResponseEntity<Map<String, String>>> getOrder(@PathVariable BigInteger id) {
        return escrowService.getOrder(id).thenApply(ResponseEntity::ok);
    }

    /** 仲裁；失败交易（例如订单不在 Disputed）会走到 exceptionally，返回 500 + 原因 */
    @PostMapping("/orders/{id}/resolve")
    public CompletableFuture<ResponseEntity<Map<String, String>>> resolve(
            @PathVariable BigInteger id,
            @RequestParam boolean sellerWins) {
        return escrowService.resolve(id, sellerWins)
                .thenCompose(receipt -> escrowService.getOrder(id).thenApply(order -> {
                    Map<String, String> body = new LinkedHashMap<>();
                    body.put("transactionHash", receipt.getTransactionHash());
                    body.put("blockNumber", receipt.getBlockNumber().toString());
                    body.put("status", order.get("status"));
                    return ResponseEntity.ok(body);
                }))
                .exceptionally(ex -> {
                    Map<String, String> body = new LinkedHashMap<>();
                    body.put("error", rootMessage(ex));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
                });
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage();
    }
}
