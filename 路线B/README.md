# 路线 B：前端托管 dapp（Next.js + scaffold-eth）

| 项目 | 内容 |
| --- | --- |
| 题目 | 大作业选做路线 B：前端托管 dapp |
| 组 / 组长 | 1 班 21 组 / 周裕杰（202430552128） |
| 代码位置 | `路线B/nextjs/`（scaffold-eth NextJS 模板改造，核心页面 `app/escrow/page.tsx`） |
| 网络 | POTOS 测试网，RPC `https://rpc-testnet.potos.hk`，浏览器 `https://scan-testnet.potos.hk` |
| 与路线 A 的关系 | 路线 A（仓库根目录 `spring-boot-web3j/`）由后端账户统一托管私钥签名；路线 B 无后端，**每个参与者用自己 MetaMask 钱包签名**，合约复用实验 3 组长部署的 Escrow |

## 1. 概述

基于 scaffold-eth NextJS 模板改造，新增 `/escrow` 页面（`app/escrow/page.tsx`，约 400 行），
直接与实验 3 组长部署的 Escrow / ZYJToken 合约交互，无需任何后端：

- **买家**：创建订单（指定卖家、代币、金额、截止时间、描述）→ `approve` + `deposit` 两步付款 → 收货后 `confirmReceipt`；对未发货订单可 `dispute` 发起争议
- **卖家**：收到 escrow 锁定的代币（买家确认后链上自动转账）
- **仲裁人**：对争议订单调用 `resolve(id, sellerWins)`，判卖家得款或判买家退款
- 页面按连接的钱包地址自动识别角色并渲染对应按钮；合约侧的角色权限（仅 arbiter 可 `resolve`、仅买家可 `confirm` / `dispute`）由 Escrow 合约强制，前端只是入口

与路线 A 的关系：路线 A 由后端账户统一托管私钥签名，路线 B 则是**每个参与者用自己钱包签名**，
更贴近真实去中心化应用的安全模型。

## 2. 环境要求与运行方法

- Node 18+、Yarn（scaffold-eth 要求，Node 20 实测通过）
- MetaMask 浏览器插件，添加 POTOS Testnet（RPC `https://rpc-testnet.potos.hk`）

```bash
cd 路线B/nextjs
yarn install        # 若报 corepack 版本错误，先执行 corepack enable
yarn start          # 监听 3000 端口
```

浏览器打开 `http://localhost:3000/escrow`，右上角 **Connect Wallet** 连接 MetaMask；
若提示 Wrong network，点击按提示添加/切换 POTOS Testnet 即可。

> 仲裁人（组员）不需要读代码，按仓库根目录《路线B-仲裁人操作说明.txt》逐步操作即可。

## 3. 合约地址与关键参数

| 项目 | 值 |
| --- | --- |
| Escrow 合约（实验 3 组长部署，路线 B 直接复用） | `0x451FD857f1bD0cD490e787455353c99049ad0B25` |
| 代币合约 ZYJToken | `0x4136723FC28Ca307eB505a0cD2F2874D792340e5` |
| 买家（前端开发，订单全流程 + 发起争议） | 张力 `0x741FCc8E590f3f3Db0610A04Ec7EB9ee79ebA62a` |
| 仲裁人（resolve 裁决） | 赖谷彪 `0x3BFd5E3fa081F2e5ba6D76D50f153BFE59A1EF7D` |
| 订单 5 卖家（真实收款验收） | 周裕杰 `0x6Cb6a3E33E75012577BDF931325eb0E521Bd12fF` |
| 订单 3 / 4 卖家 | 联调测试用第二账户 `0x68031…B25` |

`write` 类调用均设置 `disableSimulate: true`：POTOS 测试网的 `eth_call` simulate 不稳定，
会导致交易被误判为失败。

## 4. 路线 B 交易记录表

以下哈希均可在 `https://scan-testnet.potos.hk` 逐笔核验。订单 3、4 于 10 月 26 日完成，订单 5 于 10 月 30 日完成（卖家为组长真实地址，escrow 中的 10 ZYJ 已真实转入）。

| 操作 | 交易哈希 | 发起账户 |
| --- | --- | --- |
| createOrder 订单 3（10 ZYJ） | `0xe8b4136d649557b2658280172a597fe6954c64fc2cde3569e1846ce6bb2a6a96` | 买家 张力 `0x741F…A62a` |
| approve（订单 3） | `0x76b1cfeb50a30d413ae4c62a2119f6e6b4836e2caaefeccbbb1743e34d94ab0b` | 买家 张力 `0x741F…A62a` |
| deposit 订单 3 | `0x332b5093b83a8e19590ff493e95a768bcf3ed8a348f4beadf63f516724868004` | 买家 张力 `0x741F…A62a` |
| confirmReceipt 订单 3 | `0x71119546df85ce786359a99b318921c716792c6c7eb6a0e025861b9810776c9a` | 买家 张力 `0x741F…A62a` |
| createOrder 订单 4（10 ZYJ） | `0x30a169bedb34e428ddb127bdfb30cdf1d9123d3e158651f275ef692053717c62` | 买家 张力 `0x741F…A62a` |
| approve（订单 4） | `0x9442481454b7f9c83fcc1fbc671121e96e4a455ef2da8a2c4c622ece790107e3` | 买家 张力 `0x741F…A62a` |
| deposit 订单 4 | `0x420b44f763da3461fb3f72bc62c819b543f1cae33c12a3a9b8ef0c98bca681fe` | 买家 张力 `0x741F…A62a` |
| dispute 订单 4 | `0x5625ed92c74c1c15538fd07cdd160f9d1a0009c744c41a75765e57723f1246ad` | 买家 张力 `0x741F…A62a` |
| resolve 订单 4（判卖家得款） | `0x70b9983b65aa0fc9719af257303e0c0da5b59406cd67ead0d7894c07ea812d6f` | 仲裁人 赖谷彪 `0x3BFd…EF7D` |
| createOrder 订单 5（10 ZYJ） | `0x87ee646b24a6d71db3b2a6ec370061f03704d95fc41438fbb904e97f7fea6db6` | 买家 张力 `0x741F…A62a` |
| approve（订单 5） | `0x8d90b9efbdb1af495156846cc64f83e8a3b347b68df37dacea623d99ca732745` | 买家 张力 `0x741F…A62a` |
| deposit 订单 5 | `0x68fab96434751a3c79ab904b235c61a0f0c5ff97c7ceea8ec796b4b40d4f2abf` | 买家 张力 `0x741F…A62a` |
| confirmReceipt 订单 5（10 ZYJ 转入卖家） | `0xddb51d80ba1ee5218ffd8b29b876e6ea0f4101a1701e78551a205105e1e05fbf` | 买家 张力 `0x741F…A62a` |
