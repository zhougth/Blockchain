"use client";

// 大作业路线 B：去中心化托管（Escrow）前端
// 基于 scaffold-eth 2 hooks，对接组长（周裕杰）在 POTOS 测试网部署的 Escrow + ZYJToken
// Escrow:   0x451FD857f1bD0cD490e787455353c99049ad0B25
// ZYJToken: 0x4136723FC28Ca307eB505a0cD2F2874D792340e5

import { useMemo, useState } from "react";
import { formatUnits, parseUnits } from "viem";
import { useAccount } from "wagmi";
import { Address } from "~~/components/scaffold-eth";
import {
  useDeployedContractInfo,
  useScaffoldEventHistory,
  useScaffoldReadContract,
  useScaffoldWriteContract,
} from "~~/hooks/scaffold-eth";
import { notification } from "~~/utils/scaffold-eth";

const STATUS_NAMES = ["待付款", "已付款/待收货", "已完成", "已退款", "争议中"];
const STATUS_STYLES = [
  "bg-yellow-100 text-yellow-800",
  "bg-blue-100 text-blue-800",
  "bg-green-100 text-green-800",
  "bg-gray-200 text-gray-700",
  "bg-red-100 text-red-800",
];

type OrderData = {
  buyer: string;
  seller: string;
  token: string;
  amount: bigint;
  deadline: bigint;
  status: number;
  description: string;
};

const sameAddr = (a?: string, b?: string) =>
  !!a && !!b && a.toLowerCase() === b.toLowerCase();

// ---------------- 单个订单卡片 ----------------
function OrderCard({
  id,
  me,
  arbiter,
  escrowAddress,
  onChanged,
}: {
  id: number;
  me?: string;
  arbiter?: string;
  escrowAddress?: string;
  onChanged: () => void;
}) {
  const { data: rawOrder } = useScaffoldReadContract({
    contractName: "Escrow",
    functionName: "getOrder",
    args: [BigInt(id)],
  });
  const { writeContractAsync: writeEscrow, isMining } = useScaffoldWriteContract({
    contractName: "Escrow",
    disableSimulate: true,
  });
  const { writeContractAsync: writeToken } = useScaffoldWriteContract({
    contractName: "ZYJToken",
    disableSimulate: true,
  });

  if (!rawOrder) return null;
  // 兼容两种返回形态：命名对象（wagmi/viem 新版）或数组（旧版 tuple）
  const r = rawOrder as unknown as any;
  const pick = (key: string, idx: number): any =>
    r[key] !== undefined ? r[key] : r[idx];
  const order: OrderData = {
    buyer: pick("buyer", 0) as string,
    seller: pick("seller", 1) as string,
    token: pick("token", 2) as string,
    amount: pick("amount", 3) as bigint,
    deadline: pick("deadline", 4) as bigint,
    status: Number(pick("status", 5)),
    description: pick("description", 6) as string,
  };
  if (!order.amount || !order.buyer) return null;

  const isBuyer = sameAddr(me, order.buyer);
  const isSeller = sameAddr(me, order.seller);
  const isArbiter = sameAddr(me, arbiter);
  const expired = Math.floor(Date.now() / 1000) > Number(order.deadline);

  const done = (msg: string) => {
    notification.success(msg);
    onChanged();
  };

  // 付款 = 第 1 笔 approve（授权 Escrow 划款）→ 第 2 笔 deposit（合约 transferFrom 代扣）
  async function handlePay() {
    if (!escrowAddress) return;
    try {
      notification.info(`第 1/2 步：请在钱包中确认 approve，授权托管合约动用 ${formatUnits(order.amount, 18)} ZYJ`);
      await writeToken({ functionName: "approve", args: [escrowAddress, order.amount] });
      notification.info("第 2/2 步：请在钱包中确认 deposit，代币将转入托管合约");
      await writeEscrow({ functionName: "deposit", args: [BigInt(id)] });
      done(`订单 #${id} 付款成功，资金已进入托管`);
    } catch (e) {
      console.error(e);
    }
  }

  async function handle(fn: "confirm" | "refund" | "dispute", successMsg: string) {
    try {
      await writeEscrow({ functionName: fn, args: [BigInt(id)] });
      done(successMsg);
    } catch (e) {
      console.error(e);
    }
  }

  async function handleResolve(sellerWins: boolean) {
    try {
      await writeEscrow({ functionName: "resolve", args: [BigInt(id), sellerWins] });
      done(sellerWins ? `订单 #${id} 仲裁完成：款项给卖家` : `订单 #${id} 仲裁完成：款项退买家`);
    } catch (e) {
      console.error(e);
    }
  }

  return (
    <div className="card bg-base-100 shadow-xl border border-base-300">
      <div className="card-body gap-3 py-5">
        <div className="flex items-center justify-between flex-wrap gap-2">
          <h3 className="card-title text-lg">
            订单 #{id}
            <span className="text-sm font-normal text-base-content/60">{order.description}</span>
          </h3>
          <span className={`badge px-3 py-3 font-semibold ${STATUS_STYLES[order.status] ?? ""}`}>
            {STATUS_NAMES[order.status] ?? `状态${order.status}`}
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-1 text-sm">
          <div className="flex items-center gap-2">
            <span className="text-base-content/60">买家：</span>
            <Address address={order.buyer} size="sm" />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-base-content/60">卖家：</span>
            <Address address={order.seller} size="sm" />
          </div>
          <div>
            <span className="text-base-content/60">金额：</span>
            <span className="font-semibold">{formatUnits(order.amount, 18)} ZYJ</span>
          </div>
          <div>
            <span className="text-base-content/60">截止时间：</span>
            {new Date(Number(order.deadline) * 1000).toLocaleString("zh-CN")}
            {order.status === 1 && expired && <span className="text-red-500 ml-1">（已超期）</span>}
          </div>
        </div>

        {/* 按状态与角色显示操作按钮 */}
        <div className="flex flex-wrap gap-2 pt-1">
          {order.status === 0 && isBuyer && (
            <button className="btn btn-primary btn-sm" onClick={handlePay} disabled={isMining}>
              {isMining ? <span className="loading loading-spinner loading-xs" /> : null}
              付款（approve → deposit）
            </button>
          )}
          {order.status === 0 && !isBuyer && (
            <span className="text-xs text-base-content/50">等待买家付款（买家需先 approve 再 deposit）</span>
          )}

          {order.status === 1 && isBuyer && (
            <>
              <button className="btn btn-success btn-sm" onClick={() => handle("confirm", `订单 #${id} 已确认收货，款项付给卖家`)} disabled={isMining}>
                确认收货
              </button>
              {expired && (
                <button className="btn btn-warning btn-sm" onClick={() => handle("refund", `订单 #${id} 已超期退款`)} disabled={isMining}>
                  超期退款
                </button>
              )}
            </>
          )}

          {order.status === 1 && (isBuyer || isSeller) && (
            <button className="btn btn-error btn-sm" onClick={() => handle("dispute", `订单 #${id} 已发起争议，等待仲裁人处理`)} disabled={isMining}>
              发起争议
            </button>
          )}

          {order.status === 4 && isArbiter && (
            <>
              <button className="btn btn-success btn-sm" onClick={() => handleResolve(true)} disabled={isMining}>
                仲裁：判卖家得款
              </button>
              <button className="btn btn-warning btn-sm" onClick={() => handleResolve(false)} disabled={isMining}>
                仲裁：判买家退款
              </button>
            </>
          )}
          {order.status === 4 && !isArbiter && (
            <span className="text-xs text-base-content/50">争议进行中，等待仲裁人（{arbiter?.slice(0, 6)}…）裁决</span>
          )}
        </div>
      </div>
    </div>
  );
}

// ---------------- 页面主体 ----------------
export default function EscrowPage() {
  const { address: me } = useAccount();
  const { data: arbiter } = useScaffoldReadContract({
    contractName: "Escrow",
    functionName: "arbiter",
  });
  const { data: orderCount } = useScaffoldReadContract({
    contractName: "Escrow",
    functionName: "orderCount",
  });
  const { data: escrowInfo } = useDeployedContractInfo({ contractName: "Escrow" });
  const { data: tokenInfo } = useDeployedContractInfo({ contractName: "ZYJToken" });

  // POTOS 测试网 eth_getLogs 单次只扫 10 个区块，blocksBatchSize 必须为 10
  const { data: createdEvents, refetch: refetchEvents } = useScaffoldEventHistory({
    contractName: "Escrow",
    eventName: "OrderCreated",
    fromBlock: BigInt(2671313),
    watch: true,
    blocksBatchSize: 10,
  });

  const [version, setVersion] = useState(0);
  const onChanged = () => {
    setVersion(v => v + 1);
    refetchEvents();
  };

  // 订单 id 列表：orderCount 全量 + OrderCreated 事件兜底，新的在前
  const ids = useMemo(() => {
    const s = new Set<number>();
    if (orderCount !== undefined) {
      for (let i = 0; i < Number(orderCount); i++) s.add(i);
    }
    (createdEvents || []).forEach((e: any) => {
      const eid = e?.args?.id;
      if (eid !== undefined && eid !== null) s.add(Number(eid));
    });
    return Array.from(s).sort((a, b) => b - a);
  }, [orderCount, createdEvents, version]);

  // ---------- 创建订单表单 ----------
  const [seller, setSeller] = useState("");
  const [amount, setAmount] = useState("10");
  const [hours, setHours] = useState("24");
  const [description, setDescription] = useState("");
  const { writeContractAsync: writeEscrow, isMining: creating } = useScaffoldWriteContract({
    contractName: "Escrow",
    disableSimulate: true,
  });

  async function handleCreate() {
    if (!tokenInfo?.address) return;
    if (!seller.startsWith("0x") || seller.length !== 42) {
      notification.error("请填写正确的卖家地址（0x 开头 42 位）");
      return;
    }
    const amt = Number(amount);
    const hrs = Number(hours);
    if (!(amt > 0) || !(hrs > 0)) {
      notification.error("金额和期限必须为正数");
      return;
    }
    try {
      await writeEscrow({
        functionName: "createOrder",
        args: [
          seller,
          tokenInfo.address,
          parseUnits(amount, 18),
          BigInt(Math.floor(Date.now() / 1000) + hrs * 3600),
          description || "无备注",
        ],
      });
      notification.success("订单创建成功！");
      setSeller("");
      setDescription("");
      onChanged();
    } catch (e) {
      console.error(e);
    }
  }

  const isArbiter = sameAddr(me, arbiter);

  return (
    <div className="flex flex-col items-center flex-grow px-4 py-8">
      <div className="w-full max-w-3xl">
        <h1 className="text-center text-3xl font-bold mb-1">去中心化托管 Escrow</h1>
        <p className="text-center text-sm text-base-content/60 mb-4">
          买家 → 托管合约 → 卖家；超期可退款；争议由仲裁人裁决
        </p>

        {/* 合约信息 + 我的角色 */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-6">
          <div className="bg-base-200 rounded-box p-3 text-sm">
            <div className="text-base-content/60">仲裁人</div>
            <Address address={arbiter as string | undefined} size="sm" />
          </div>
          <div className="bg-base-200 rounded-box p-3 text-sm">
            <div className="text-base-content/60">Escrow 合约</div>
            <Address address={escrowInfo?.address} size="sm" />
          </div>
          <div className="bg-base-200 rounded-box p-3 text-sm">
            <div className="text-base-content/60">我的角色</div>
            <div className="font-semibold mt-1">
              {!me && "请先连接钱包"}
              {me && isArbiter && <span className="text-red-500">仲裁人（可裁决争议）</span>}
              {me && !isArbiter && <span className="text-xs break-all">{me}</span>}
            </div>
          </div>
        </div>

        {/* 创建订单 */}
        <div className="card bg-base-100 shadow-xl border border-base-300 mb-6">
          <div className="card-body py-5 gap-3">
            <h2 className="card-title text-lg">创建订单（买家发起）</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <label className="form-control">
                <span className="label-text pb-1">卖家地址</span>
                <input
                  className="input input-bordered input-sm w-full"
                  placeholder="0x..."
                  value={seller}
                  onChange={e => setSeller(e.target.value)}
                />
              </label>
              <label className="form-control">
                <span className="label-text pb-1">金额（ZYJ）</span>
                <input className="input input-bordered input-sm w-full" value={amount} onChange={e => setAmount(e.target.value)} />
              </label>
              <label className="form-control">
                <span className="label-text pb-1">付款截止（小时后）</span>
                <input className="input input-bordered input-sm w-full" value={hours} onChange={e => setHours(e.target.value)} />
              </label>
              <label className="form-control">
                <span className="label-text pb-1">订单描述</span>
                <input
                  className="input input-bordered input-sm w-full"
                  placeholder="例如：购买二手书"
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                />
              </label>
            </div>
            <button className="btn btn-primary btn-sm w-40" onClick={handleCreate} disabled={creating || !me}>
              {creating ? <span className="loading loading-spinner loading-xs" /> : null}
              创建订单
            </button>
            {!me && <span className="text-xs text-base-content/50">需要先连接钱包才能创建订单</span>}
          </div>
        </div>

        {/* 订单列表 */}
        <h2 className="text-xl font-bold mb-3">
          全部订单（{ids.length}）<span className="text-sm font-normal text-base-content/50">新订单在前</span>
        </h2>
        <div className="flex flex-col gap-4">
          {ids.map(id => (
            <OrderCard
              key={`${id}-${version}`}
              id={id}
              me={me}
              arbiter={arbiter as string | undefined}
              escrowAddress={escrowInfo?.address}
              onChanged={onChanged}
            />
          ))}
          {ids.length === 0 && (
            <div className="text-center text-base-content/50 py-8 bg-base-200 rounded-box">暂无订单，先创建一个吧</div>
          )}
        </div>
      </div>
    </div>
  );
}
