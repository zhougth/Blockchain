package com.wetech.demo.web3j.contracts.escrow;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.14.0.
 */
@SuppressWarnings("rawtypes")
public class Escrow extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b5060405161143f38038061143f833981016040819052602b9160b3565b6001600160a01b03811660905760405162461bcd60e51b815260206004820152602360248201527f457363726f773a206172626974657220697320746865207a65726f206164647260448201526265737360e81b606482015260840160405180910390fd5b5f80546001600160a01b0319166001600160a01b039290921691909117905560de565b5f6020828403121560c2575f5ffd5b81516001600160a01b038116811460d7575f5ffd5b9392505050565b611354806100eb5f395ff3fe608060405234801561000f575f5ffd5b5060043610610090575f3560e01c806386d6282c1161006357806386d6282c146100eb578063b6b55f25146100fe578063ba0179b514610111578063d09ef24114610124578063fe25e00a14610144575f5ffd5b806302bd3421146100945780632453ffa8146100ba578063278ecde1146100c357806352a34b05146100d8575b5f5ffd5b6100a76100a2366004610ec6565b61016e565b6040519081526020015b60405180910390f35b6100a760015481565b6100d66100d1366004610fac565b610431565b005b6100d66100e6366004610fd3565b610620565b6100d66100f9366004610fac565b6108ba565b6100d661010c366004610fac565b6109d5565b6100d661011f366004610fac565b610b74565b610137610132366004610fac565b610ce0565b6040516100b19190611063565b5f54610156906001600160a01b031681565b6040516001600160a01b0390911681526020016100b1565b5f6001600160a01b0386166101d55760405162461bcd60e51b815260206004820152602260248201527f457363726f773a2073656c6c657220697320746865207a65726f206164647265604482015261737360f01b60648201526084015b60405180910390fd5b336001600160a01b0387160361022d5760405162461bcd60e51b815260206004820152601b60248201527f457363726f773a2073656c6c657220697320746865206275796572000000000060448201526064016101cc565b5f841161027c5760405162461bcd60e51b815260206004820152601f60248201527f457363726f773a20616d6f756e74206d75737420626520706f7369746976650060448201526064016101cc565b4283116102da5760405162461bcd60e51b815260206004820152602660248201527f457363726f773a20646561646c696e65206d75737420626520696e207468652060448201526566757475726560d01b60648201526084016101cc565b600180546040805160e0810182523381526001600160a01b038a811660208084019182528b8316848601908152606085018c8152608086018c81525f60a0880181815260c089018e90528a8252600295869052989020875181549088166001600160a01b03199182161782559551818c018054918916918816919091179055925193830180549490961693909416929092179093555160038301555160048083019190915592516005820180549596939592949193909260ff19169184908111156103a7576103a7611001565b021790555060c082015160068201906103c09082611172565b5050600180546103d1925090611231565b600155604080516001600160a01b0388811682526020820188905291810186905290881690339083907fa4be1e061958d160de04db54668a64922c36d2765b16644ae91acf127708970e9060600160405180910390a49695505050505050565b5f8181526002602052604090205481906001600160a01b031633146104685760405162461bcd60e51b81526004016101cc90611256565b816001805f8381526002602052604090206005015460ff16600481111561049157610491611001565b146104ae5760405162461bcd60e51b81526004016101cc9061128d565b5f8481526002602052604090206004810154421161050e5760405162461bcd60e51b815260206004820152601c60248201527f457363726f773a20646561646c696e65206e6f7420726561636865640000000060448201526064016101cc565b60058101805460ff19166003908117909155600282015482549183015460405163a9059cbb60e01b81526001600160a01b03938416600482015260248101919091525f929091169063a9059cbb906044016020604051808303815f875af115801561057b573d5f5f3e3d5ffd5b505050506040513d601f19601f8201168201806040525081019061059f91906112bb565b9050806105ee5760405162461bcd60e51b815260206004820152601e60248201527f457363726f773a20726566756e64207472616e73666572206661696c6564000060448201526064016101cc565b60405186907f303a31648d19f17852cf82657162c4bdd4a06ab3f36f1e7ae1118f4ca6473e11905f90a2505050505050565b5f546001600160a01b031633146106835760405162461bcd60e51b815260206004820152602160248201527f457363726f773a2063616c6c6572206973206e6f7420746865206172626974656044820152603960f91b60648201526084016101cc565b816004805f8381526002602052604090206005015460ff1660048111156106ac576106ac611001565b146106c95760405162461bcd60e51b81526004016101cc9061128d565b5f84815260026020526040902083156107955760058101805460ff191660029081179091558101546001820154600383015460405163a9059cbb60e01b81526001600160a01b03928316600482015260248101919091525f92919091169063a9059cbb906044016020604051808303815f875af115801561074c573d5f5f3e3d5ffd5b505050506040513d601f19601f8201168201806040525081019061077091906112bb565b90508061078f5760405162461bcd60e51b81526004016101cc906112dd565b50610877565b60058101805460ff19166003908117909155600282015482549183015460405163a9059cbb60e01b81526001600160a01b03938416600482015260248101919091525f929091169063a9059cbb906044016020604051808303815f875af1158015610802573d5f5f3e3d5ffd5b505050506040513d601f19601f8201168201806040525081019061082691906112bb565b9050806108755760405162461bcd60e51b815260206004820152601e60248201527f457363726f773a20726566756e64207472616e73666572206661696c6564000060448201526064016101cc565b505b847f649ff37700d4eb8a5bc86ef06a55183597fa5c3c6b53ec4b9ee510cb2089ee26856040516108ab911515815260200190565b60405180910390a25050505050565b806001805f8381526002602052604090206005015460ff1660048111156108e3576108e3611001565b146109005760405162461bcd60e51b81526004016101cc9061128d565b5f83815260026020526040902080546001600160a01b0316331480610931575060018101546001600160a01b031633145b61098b5760405162461bcd60e51b815260206004820152602560248201527f457363726f773a2063616c6c6572206973206e6f74206275796572206f72207360448201526432b63632b960d91b60648201526084016101cc565b60058101805460ff1916600417905560405133815284907fde481be1936299e9233634e9f3cd92028f797634de15defa790fb2d7a013c95a9060200160405180910390a250505050565b5f8181526002602052604090205481906001600160a01b03163314610a0c5760405162461bcd60e51b81526004016101cc90611256565b815f805f8381526002602052604090206005015460ff166004811115610a3457610a34611001565b14610a515760405162461bcd60e51b81526004016101cc9061128d565b5f84815260026020819052604080832060058101805460ff19166001179055918201548254600384015492516323b872dd60e01b81526001600160a01b039182166004820152306024820152604481019390935292939216906323b872dd906064016020604051808303815f875af1158015610acf573d5f5f3e3d5ffd5b505050506040513d601f19601f82011682018060405250810190610af391906112bb565b905080610b425760405162461bcd60e51b815260206004820152601b60248201527f457363726f773a207472616e7366657246726f6d206661696c6564000000000060448201526064016101cc565b60405186907f15e33efda35e5da2fb096ba49585307ba468af144aea691da964aaff1a7f3a57905f90a2505050505050565b5f8181526002602052604090205481906001600160a01b03163314610bab5760405162461bcd60e51b81526004016101cc90611256565b816001805f8381526002602052604090206005015460ff166004811115610bd457610bd4611001565b14610bf15760405162461bcd60e51b81526004016101cc9061128d565b5f84815260026020819052604080832060058101805460ff1916841790559182015460018301546003840154925163a9059cbb60e01b81526001600160a01b0391821660048201526024810193909352929392169063a9059cbb906044016020604051808303815f875af1158015610c6b573d5f5f3e3d5ffd5b505050506040513d601f19601f82011682018060405250810190610c8f91906112bb565b905080610cae5760405162461bcd60e51b81526004016101cc906112dd565b60405186907f02a7d4d9472af7118644884b9a0ee443540c0027e938dd0aa35be8ecbe946c0a905f90a2505050505050565b610d1f6040805160e0810182525f8082526020820181905291810182905260608101829052608081018290529060a08201908152602001606081525090565b6001548210610d705760405162461bcd60e51b815260206004820152601c60248201527f457363726f773a206f7264657220646f6573206e6f742065786973740000000060448201526064016101cc565b5f82815260026020818152604092839020835160e08101855281546001600160a01b03908116825260018301548116938201939093529281015490911692820192909252600382015460608201526004808301546080830152600583015491929160a084019160ff90911690811115610deb57610deb611001565b6004811115610dfc57610dfc611001565b8152602001600682018054610e10906110e3565b80601f0160208091040260200160405190810160405280929190818152602001828054610e3c906110e3565b8015610e875780601f10610e5e57610100808354040283529160200191610e87565b820191905f5260205f20905b815481529060010190602001808311610e6a57829003601f168201915b5050505050815250509050919050565b80356001600160a01b0381168114610ead575f5ffd5b919050565b634e487b7160e01b5f52604160045260245ffd5b5f5f5f5f5f60a08688031215610eda575f5ffd5b610ee386610e97565b9450610ef160208701610e97565b93506040860135925060608601359150608086013567ffffffffffffffff811115610f1a575f5ffd5b8601601f81018813610f2a575f5ffd5b803567ffffffffffffffff811115610f4457610f44610eb2565b604051601f8201601f19908116603f0116810167ffffffffffffffff81118282101715610f7357610f73610eb2565b6040528181528282016020018a1015610f8a575f5ffd5b816020840160208301375f602083830101528093505050509295509295909350565b5f60208284031215610fbc575f5ffd5b5035919050565b8015158114610fd0575f5ffd5b50565b5f5f60408385031215610fe4575f5ffd5b823591506020830135610ff681610fc3565b809150509250929050565b634e487b7160e01b5f52602160045260245ffd5b6005811061103157634e487b7160e01b5f52602160045260245ffd5b9052565b5f81518084528060208401602086015e5f602082860101526020601f19601f83011685010191505092915050565b6020815260018060a01b03825116602082015260018060a01b03602083015116604082015260018060a01b03604083015116606082015260608201516080820152608082015160a08201525f60a08301516110c160c0840182611015565b5060c083015160e0808401526110db610100840182611035565b949350505050565b600181811c908216806110f757607f821691505b60208210810361111557634e487b7160e01b5f52602260045260245ffd5b50919050565b601f82111561116d578282111561116d57805f5260205f20601f840160051c602085101561114657505f5b90810190601f840160051c035f5b81811015611169575f83820155600101611154565b5050505b505050565b815167ffffffffffffffff81111561118c5761118c610eb2565b6111a08161119a84546110e3565b8461111b565b6020601f8211600181146111d2575f83156111bb5750848201515b5f19600385901b1c1916600184901b17845561122a565b5f84815260208120601f198516915b8281101561120157878501518255602094850194600190920191016111e1565b508482101561121e57868401515f19600387901b60f8161c191681555b505060018360011b0184555b5050505050565b8082018082111561125057634e487b7160e01b5f52601160045260245ffd5b92915050565b6020808252601f908201527f457363726f773a2063616c6c6572206973206e6f742074686520627579657200604082015260600190565b602080825260149082015273457363726f773a2077726f6e672073746174757360601b604082015260600190565b5f602082840312156112cb575f5ffd5b81516112d681610fc3565b9392505050565b60208082526021908201527f457363726f773a207472616e7366657220746f2073656c6c6572206661696c656040820152601960fa1b60608201526080019056fea264697066735822122049e7f6c8b61f9167abb3f1074b838bb353b7a934b2f7f0cf1fa4e3fca28db13d64736f6c63430008230033";

    private static String librariesLinkedBinary;

    public static final String FUNC_ARBITER = "arbiter";

    public static final String FUNC_CONFIRM = "confirm";

    public static final String FUNC_CREATEORDER = "createOrder";

    public static final String FUNC_DEPOSIT = "deposit";

    public static final String FUNC_DISPUTE = "dispute";

    public static final String FUNC_GETORDER = "getOrder";

    public static final String FUNC_ORDERCOUNT = "orderCount";

    public static final String FUNC_REFUND = "refund";

    public static final String FUNC_RESOLVE = "resolve";

    public static final Event ORDERCOMPLETED_EVENT = new Event("OrderCompleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event ORDERCREATED_EVENT = new Event("OrderCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ORDERDISPUTED_EVENT = new Event("OrderDisputed", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>() {}));
    ;

    public static final Event ORDERPAID_EVENT = new Event("OrderPaid", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event ORDERREFUNDED_EVENT = new Event("OrderRefunded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event ORDERRESOLVED_EVENT = new Event("OrderResolved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Bool>() {}));
    ;

    @Deprecated
    protected Escrow(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Escrow(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Escrow(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Escrow(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<OrderCompletedEventResponse> getOrderCompletedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ORDERCOMPLETED_EVENT, transactionReceipt);
        ArrayList<OrderCompletedEventResponse> responses = new ArrayList<OrderCompletedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OrderCompletedEventResponse typedResponse = new OrderCompletedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OrderCompletedEventResponse getOrderCompletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ORDERCOMPLETED_EVENT, log);
        OrderCompletedEventResponse typedResponse = new OrderCompletedEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<OrderCompletedEventResponse> orderCompletedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOrderCompletedEventFromLog(log));
    }

    public Flowable<OrderCompletedEventResponse> orderCompletedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ORDERCOMPLETED_EVENT));
        return orderCompletedEventFlowable(filter);
    }

    public static List<OrderCreatedEventResponse> getOrderCreatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ORDERCREATED_EVENT, transactionReceipt);
        ArrayList<OrderCreatedEventResponse> responses = new ArrayList<OrderCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OrderCreatedEventResponse typedResponse = new OrderCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.token = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OrderCreatedEventResponse getOrderCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ORDERCREATED_EVENT, log);
        OrderCreatedEventResponse typedResponse = new OrderCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.token = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<OrderCreatedEventResponse> orderCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOrderCreatedEventFromLog(log));
    }

    public Flowable<OrderCreatedEventResponse> orderCreatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ORDERCREATED_EVENT));
        return orderCreatedEventFlowable(filter);
    }

    public static List<OrderDisputedEventResponse> getOrderDisputedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ORDERDISPUTED_EVENT, transactionReceipt);
        ArrayList<OrderDisputedEventResponse> responses = new ArrayList<OrderDisputedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OrderDisputedEventResponse typedResponse = new OrderDisputedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.by = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OrderDisputedEventResponse getOrderDisputedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ORDERDISPUTED_EVENT, log);
        OrderDisputedEventResponse typedResponse = new OrderDisputedEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.by = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<OrderDisputedEventResponse> orderDisputedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOrderDisputedEventFromLog(log));
    }

    public Flowable<OrderDisputedEventResponse> orderDisputedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ORDERDISPUTED_EVENT));
        return orderDisputedEventFlowable(filter);
    }

    public static List<OrderPaidEventResponse> getOrderPaidEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ORDERPAID_EVENT, transactionReceipt);
        ArrayList<OrderPaidEventResponse> responses = new ArrayList<OrderPaidEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OrderPaidEventResponse typedResponse = new OrderPaidEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OrderPaidEventResponse getOrderPaidEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ORDERPAID_EVENT, log);
        OrderPaidEventResponse typedResponse = new OrderPaidEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<OrderPaidEventResponse> orderPaidEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOrderPaidEventFromLog(log));
    }

    public Flowable<OrderPaidEventResponse> orderPaidEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ORDERPAID_EVENT));
        return orderPaidEventFlowable(filter);
    }

    public static List<OrderRefundedEventResponse> getOrderRefundedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ORDERREFUNDED_EVENT, transactionReceipt);
        ArrayList<OrderRefundedEventResponse> responses = new ArrayList<OrderRefundedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OrderRefundedEventResponse typedResponse = new OrderRefundedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OrderRefundedEventResponse getOrderRefundedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ORDERREFUNDED_EVENT, log);
        OrderRefundedEventResponse typedResponse = new OrderRefundedEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<OrderRefundedEventResponse> orderRefundedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOrderRefundedEventFromLog(log));
    }

    public Flowable<OrderRefundedEventResponse> orderRefundedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ORDERREFUNDED_EVENT));
        return orderRefundedEventFlowable(filter);
    }

    public static List<OrderResolvedEventResponse> getOrderResolvedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ORDERRESOLVED_EVENT, transactionReceipt);
        ArrayList<OrderResolvedEventResponse> responses = new ArrayList<OrderResolvedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OrderResolvedEventResponse typedResponse = new OrderResolvedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.sellerWins = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OrderResolvedEventResponse getOrderResolvedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ORDERRESOLVED_EVENT, log);
        OrderResolvedEventResponse typedResponse = new OrderResolvedEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.sellerWins = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<OrderResolvedEventResponse> orderResolvedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOrderResolvedEventFromLog(log));
    }

    public Flowable<OrderResolvedEventResponse> orderResolvedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ORDERRESOLVED_EVENT));
        return orderResolvedEventFlowable(filter);
    }

    public RemoteFunctionCall<String> arbiter() {
        final Function function = new Function(FUNC_ARBITER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> confirm(BigInteger id) {
        final Function function = new Function(
                FUNC_CONFIRM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createOrder(String seller, String token,
            BigInteger amount, BigInteger deadline, String description) {
        final Function function = new Function(
                FUNC_CREATEORDER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, seller), 
                new org.web3j.abi.datatypes.Address(160, token), 
                new org.web3j.abi.datatypes.generated.Uint256(amount), 
                new org.web3j.abi.datatypes.generated.Uint256(deadline), 
                new org.web3j.abi.datatypes.Utf8String(description)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> deposit(BigInteger id) {
        final Function function = new Function(
                FUNC_DEPOSIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> dispute(BigInteger id) {
        final Function function = new Function(
                FUNC_DISPUTE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Order> getOrder(BigInteger id) {
        final Function function = new Function(FUNC_GETORDER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Order>() {}));
        return executeRemoteCallSingleValueReturn(function, Order.class);
    }

    public RemoteFunctionCall<BigInteger> orderCount() {
        final Function function = new Function(FUNC_ORDERCOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> refund(BigInteger id) {
        final Function function = new Function(
                FUNC_REFUND, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> resolve(BigInteger id, Boolean sellerWins) {
        final Function function = new Function(
                FUNC_RESOLVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id), 
                new org.web3j.abi.datatypes.Bool(sellerWins)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Escrow load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new Escrow(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Escrow load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Escrow(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Escrow load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new Escrow(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Escrow load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Escrow(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Escrow> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider, String _arbiter) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _arbiter)));
        return deployRemoteCall(Escrow.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    public static RemoteCall<Escrow> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider, String _arbiter) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _arbiter)));
        return deployRemoteCall(Escrow.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Escrow> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, String _arbiter) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _arbiter)));
        return deployRemoteCall(Escrow.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Escrow> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit, String _arbiter) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _arbiter)));
        return deployRemoteCall(Escrow.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class Order extends DynamicStruct {
        public String buyer;

        public String seller;

        public String token;

        public BigInteger amount;

        public BigInteger deadline;

        public BigInteger status;

        public String description;

        public Order(String buyer, String seller, String token, BigInteger amount,
                BigInteger deadline, BigInteger status, String description) {
            super(new org.web3j.abi.datatypes.Address(160, buyer), 
                    new org.web3j.abi.datatypes.Address(160, seller), 
                    new org.web3j.abi.datatypes.Address(160, token), 
                    new org.web3j.abi.datatypes.generated.Uint256(amount), 
                    new org.web3j.abi.datatypes.generated.Uint256(deadline), 
                    new org.web3j.abi.datatypes.generated.Uint8(status), 
                    new org.web3j.abi.datatypes.Utf8String(description));
            this.buyer = buyer;
            this.seller = seller;
            this.token = token;
            this.amount = amount;
            this.deadline = deadline;
            this.status = status;
            this.description = description;
        }

        public Order(Address buyer, Address seller, Address token, Uint256 amount, Uint256 deadline,
                Uint8 status, Utf8String description) {
            super(buyer, seller, token, amount, deadline, status, description);
            this.buyer = buyer.getValue();
            this.seller = seller.getValue();
            this.token = token.getValue();
            this.amount = amount.getValue();
            this.deadline = deadline.getValue();
            this.status = status.getValue();
            this.description = description.getValue();
        }
    }

    public static class OrderCompletedEventResponse extends BaseEventResponse {
        public BigInteger id;
    }

    public static class OrderCreatedEventResponse extends BaseEventResponse {
        public BigInteger id;

        public String buyer;

        public String seller;

        public String token;

        public BigInteger amount;

        public BigInteger deadline;
    }

    public static class OrderDisputedEventResponse extends BaseEventResponse {
        public BigInteger id;

        public String by;
    }

    public static class OrderPaidEventResponse extends BaseEventResponse {
        public BigInteger id;
    }

    public static class OrderRefundedEventResponse extends BaseEventResponse {
        public BigInteger id;
    }

    public static class OrderResolvedEventResponse extends BaseEventResponse {
        public BigInteger id;

        public Boolean sellerWins;
    }
}
