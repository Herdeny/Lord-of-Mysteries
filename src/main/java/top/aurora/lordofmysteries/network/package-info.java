/**
 * 网络包 (§16.1)：Forge 1.20.1 SimpleChannel。
 *
 * <p>C2S 控制包由客户端按键触发，服务端才执行灵性扣费/结果计算，
 * 避免客户端伪造。批次1 只包含灵视开关和简易占卜触发；S2C 结果面板留到 M2 拆出。
 */
package top.aurora.lordofmysteries.network;
