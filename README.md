# 2026《区块链技术与应用》大作业 · Escrow 托管平台（路线 A 后端 + 路线 B 前端）

| 项目 | 内容 |
| --- | --- |
| 题目 | 在区块链上构建应用系统（必做：路线 A 平台后端；选做：路线 B 前端托管 dapp） |
| 组 / 组长 | 1 班 21 组 / 周裕杰（202430552128） |
| 网络 | POTOS 测试网，RPC `https://rpc-testnet.potos.hk`，浏览器 `https://scan-testnet.potos.hk` |
| 仓库 | `https://github.com/zhougth/Blockchain.git`（分支 `main`） |
| 路线 A | Java 21 + Spring Boot 3.2.3 + Web3j 4.14.0，代码在 `spring-boot-web3j/`（见第 1~8 节） |
| 路线 B | Next.js（scaffold-eth）前端托管 dapp，代码在 `路线B/` 目录（独立 README） |

- **路线 A**：后端账户充当**仲裁人**，私钥只从环境变量注入；买家 A、卖家 B 在 Remix 里用 `At Address` 加载后端部署的 Escrow 操作。
- **路线 B**：复用实验 3 组长部署的 Escrow 合约（`0x451F…0B25`），前端 `/escrow` 页面实现订单列表 / 创建 / 付款 / 确认收货 / 发起争议 / 仲裁裁决，买家、卖家、仲裁人三种角色通过 MetaMask 直接签名交互。

---

## 1. 目录结构

```
blockchain/
├── README.md                     ← 本文件：如何运行 / 环境变量 / 合约地址 / 交易记录表
├── spring-boot-web3j/            ← 路线 A：平台后端（Java + Web3j）
│   ├── build.gradle              ← web3j 插件 + sourceSets.solidity 指向 resources/contracts
│   └── src/main/
│       ├── java/com/wetech/demo/web3j/
│       │   ├── Application.java
│       │   ├── config/Web3jConfig.java                 Web3j / Credentials / ContractGasProvider
│       │   ├── controller/EscrowController.java        6 个必做接口
│       │   ├── controller/SimpleStorageController.java 示例接口（4.4 跑通用）
│       │   ├── service/EscrowService.java              deploy / load / getOrder / listOrders / resolve
│       │   └── contracts/{escrow,zyjtoken,ierc20,simplestorage}/   generateContractWrappers 生成
│       └── resources/
│           ├── application.properties                  私钥用占位符，见第 3 节
│           └── contracts/{Escrow.sol, ZYJToken.sol, SimpleStorage.sol}
└── 路线B/                        ← 路线 B：前端托管 dapp（与路线 A 完全独立）
    ├── README.md                 ← 路线 B 的运行方法 / 合约地址 / 交易记录表
    ├── nextjs/                   ← scaffold-eth NextJS 工程，核心页面 app/escrow/page.tsx
    └── 路线B-仲裁人操作说明.txt    ← 给仲裁人（组员）的一页纸操作指南
```

`Escrow.sol`、`ZYJToken.sol` 与实验 3 组长部署用的源码一致。

## 2. 环境要求

- JDK 21（`build.gradle` 里 `sourceCompatibility = '21'`）
- WSL Ubuntu，项目放在 `~/blockchain`
- Gradle 用仓库自带的 wrapper，不需要本机安装 gradle
- 三个测试网账户（MetaMask + 水龙头领 POT）：后端账户（=仲裁人）、买家 A、卖家 B

## 3. 需要的环境变量

| 变量 | 含义 | 值从哪来 |
| --- | --- | --- |
| `WEB3J_PRIVATE_KEY` | 后端账户（仲裁人）私钥，`0x` 开头的 64 位十六进制 | MetaMask 中该账户导出，**只填在自己的终端里，仓库不存** |

`application.properties` 中引用它，仓库里永远只是占位符：

```properties
web3j.client-address=https://rpc-testnet.potos.hk
web3j.private-key=${WEB3J_PRIVATE_KEY}
web3j.gas-price=1000000
web3j.gas-limit=6721975
```

`gas-price` 必须显式设小：POTOS 测试网 gas price 量级只有约 21000 wei，示例仓库默认的 20 gwei 会多付上百万倍。`gas-limit` 用 6721975 以保证 `createOrder` / `deposit` 这类带事件与转账的调用不被截断。

启动前在终端导出（**本仓库与文档中均不出现真实值**）：

```bash
export WEB3J_PRIVATE_KEY=0x<在这里填你自己的私钥>
```

用 IDEA 运行时，在 Run Configuration → Environment variables 里填同名键值；否则 `bootRun` 会报 `Could not resolve placeholder 'WEB3J_PRIVATE_KEY'`。

## 4. 如何运行

```bash
cd ~/blockchain/spring-boot-web3j

# （可选）改了 resources/contracts 下的 .sol 之后重新生成 Java 封装
bash gradlew generateContractWrappers

# 启动后端，监听 8080
bash gradlew bootRun
```

启动日志会打印 `Connecting to Ethereum client: https://rpc-testnet.potos.hk`。

## 5. REST 接口

服务监听 `http://localhost:8080`，共 6 个必做接口：

| 方法 | 路径 | 参数 | 返回 |
| --- | --- | --- | --- |
| POST | `/api/escrow/deploy` | 无 | `contractAddress`、`transactionHash`、`deployBlock` |
| POST | `/api/escrow/load` | `address`、`fromBlock` | `contractAddress` |
| GET | `/api/escrow/address` | 无 | `contractAddress`、`arbiter` |
| GET | `/api/escrow/orders` | 无 | 数组，每项 `id`、`buyer`、`seller`、`amount`、`blockNumber`、`transactionHash`（来自 `OrderCreated` 事件） |
| GET | `/api/escrow/orders/{id}` | 无 | `buyer`、`seller`、`token`、`amount`、`deadline`、`status`（文字）、`description` |
| POST | `/api/escrow/orders/{id}/resolve` | `sellerWins` | `transactionHash`、`blockNumber`、`status`；失败时返回 HTTP 500 + `error` |



## 6. 合约地址与关键参数

| 项目 | 值 |
| --- | --- |
| 后端账户（= 仲裁人） | `0xDD73B970A5f9E3179330956EFA4EC9315CB42a89` |
| 买家 A | `0x3BFd5E3fa081F2e5ba6D76D50f153Bfe59A1EF7D` |
| 卖家 B | `0x741FCc8E590f3f3Db0610A04Ec7EB9ee79ebA62a` |
| 路线 A 后端部署的 Escrow | `0x901EDE62a702A23AdDE0E31c19d334536c9A4668` |
| 上述 Escrow 的部署区块（`load` 的 `fromBlock`） | `2689563` |
| 代币合约 ZYJToken | `0x4136723FC28Ca307eB505a0cD2F2874D792340e5`（实验 3 组长部署，路线 A 未重新部署） |
| 代币元数据 | `name = ZYJToken`，`symbol = ZYJ202430552128`，`decimals = 18` |
| 验收订单 | 两笔，均为 30 个（= `30000000000000000000` wei）、`deadline = 1793008248` |



## 7. 交易记录表


| 操作 | 交易哈希 | 区块浏览器链接 | 发起账户 |
| --- | --- | --- | --- |
| 部署 Escrow | `0x9c978de0ea896c831a9c0017df4ab19774ed8778df2b24f7d3a925bf930c4a4e` | https://scan-testnet.potos.hk/tx/0x9c978de0ea896c831a9c0017df4ab19774ed8778df2b24f7d3a925bf930c4a4e | 后端账户（仲裁人）`0xDD73…2a89` |
| createOrder 订单 0 | `0x72a3ee3310b6f04fdbd674192bf802ec79d7af0a385e3f60f2fadbb767417ccc` | https://scan-testnet.potos.hk/tx/0x72a3ee3310b6f04fdbd674192bf802ec79d7af0a385e3f60f2fadbb767417ccc | 买家 A `0x3BFd…1EF7D` |
| approve | `0x772bc45d1be340b9cd8325974b87cfd1fd967f483e84b89db37da1b96cba7c97` | https://scan-testnet.potos.hk/tx/0x772bc45d1be340b9cd8325974b87cfd1fd967f483e84b89db37da1b96cba7c97 | 买家 A `0x3BFd…1EF7D` |
| deposit 订单 0 | `0xeb5d547dcad057aa29b3883590b87ab975ab7059dae2489e26817ff89729bd95` | https://scan-testnet.potos.hk/tx/0xeb5d547dcad057aa29b3883590b87ab975ab7059dae2489e26817ff89729bd95 | 买家 A `0x3BFd…1EF7D` |
| dispute 订单 1 | `0x2a8cc05f1c616afdea5fb7cc414a06855e40b6663d01293b87a350016af82ac8` | https://scan-testnet.potos.hk/tx/0x2a8cc05f1c616afdea5fb7cc414a06855e40b6663d01293b87a350016af82ac8 | 卖家 B `0x741F…A62a` |
| resolve 订单 1 | `0xf38b794c63500fb9e69db0f0465f63327f88c899756442b8369b60781b73446f` | https://scan-testnet.potos.hk/tx/0xf38b794c63500fb9e69db0f0465f63327f88c899756442b8369b60781b73446f | 后端账户（仲裁人）`0xDD73…2a89` |
| confirm 订单 0 | `0x75b7b21511686576d186b2c7225dba373fb9dbe2d027514a71868855cc233123` | https://scan-testnet.potos.hk/tx/0x75b7b21511686576d186b2c7225dba373fb9dbe2d027514a71868855cc233123 | 买家 A `0x3BFd…1EF7D` |
| resolve 失败（第 6 步） | `0xdaf02d9010055b78a589b535932d3609e3e526bf13ad5d834b3afe3b377c6167` | https://scan-testnet.potos.hk/tx/0xdaf02d9010055b78a589b535932d3609e3e526bf13ad5d834b3afe3b377c6167 | 后端账户（仲裁人）`0xDD73…2a89` |

---

## 8. 路线 B（选做）

路线 B 的全部内容（前端工程、运行方法、合约地址、交易记录表、仲裁人操作说明）已独立放在 [`路线B/`](./路线B/README.md) 目录，与路线 A 互不干扰。
