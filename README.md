# AuthView

基于 **[Blink](https://github.com/17Artist/Blink)** 框架 + **[ArcartX](https://github.com/17Artist/ArcartX_Plugin)** UI 系统的 **AuthMe 登录 / 注册 / 改密界面**插件。

---

## 依赖

| 依赖      | 版本                                          | 必需    |
|---------|---------------------------------------------|-------|
| ArcartX | 2.x（开发针对 2.2.0.5）                           | ✅ 硬依赖 |
| AuthMe  | 5.6+ / 5.7.x / 6.0（开发针对 5.7.0 API，跨版本二进制兼容） | ✅ 硬依赖 |
| 服务端     | Spigot / Paper 1.18 及以上                     | ✅     |


---

## 安装

1. 把 `AuthView-<version>.jar` 放进 `plugins/`。
2. 确保已安装并启用 ArcartX 与 AuthMe。
3. 启动服务器，插件会在 `plugins/AuthView/` 下生成：
   - `config.yml` — 行为配置
   - `language.yml` — 语言文件
   - `view/auth_view.yml` — 界面布局

---

## 命令

根命令 `/arcartxauthview`，别名 `/aav`、`/authview`。

| 命令              | 说明         | 权限                | 发送者 |
|-----------------|------------|-------------------|-----|
| `/aav reload`   | 重载配置、语言与界面 | `authview.reload` | OP  |
| `/aav login`    | 打开登录界面     | —                 | 玩家  |
| `/aav register` | 打开注册界面     | —                 | 玩家  |
| `/aav change`   | 打开修改密码界面   | —                 | 玩家  |

> 正常玩家无需用命令，进服会自动弹界面。`/aav change` 供已登录玩家自助改密（需输入原密码校验）。

---

## 配置 `config.yml`

| 键                     | 默认     | 说明                           |
|-----------------------|--------|------------------------------|
| `enable-auto-open`    | `true` | 客户端就绪后，未登录玩家是否自动弹界面          |
| `bungee-grace-ticks`  | `40`   | 等待跨服自动登录的宽限时间（tick，20=1 秒）   |
| `max-login-attempts`  | `5`    | 界面登录允许的最大连续错误次数，超过踢出；`0` 不限制 |
| `min-password-length` | `4`    | 注册 / 改密最小密码长度（界面侧校验）         |
| `max-password-length` | `30`   | 注册 / 改密最大密码长度，`0` 不限制        |
| `lock-extra-slot`     | `true` | 未登录时禁止点击 ArcartX 额外槽位        |

`language.yml` 内为全部提示文案与界面标题 / 副标题，支持 `&` 颜色代码（界面文本由 ArcartX 客户端直接渲染）。`%min%` / `%max%` 会被替换为对应长度。

---

## 界面通信约定

服务端 → 客户端（`AuthUI.kt`）：

| 包          | 数据                             | 作用             |
|------------|--------------------------------|----------------|
| `init`     | `{mode, serverName, subtitle}` | 初始化界面模式与标题     |
| `feedback` | `{message, ok:false}`          | 失败提示 + 面板抖动    |
| `success`  | `{message}`                    | 成功提示，客户端稍后优雅关闭 |

客户端 → 服务端（`view/auth_view.yml`，`Packet.send`）：

| 包          | 参数                |
|------------|-------------------|
| `login`    | `密码`              |
| `register` | `密码, 确认密码`        |
| `change`   | `原密码, 新密码, 确认新密码` |

想改界面外观，直接编辑 `plugins/AuthView/view/auth_view.yml` 后 `/aav reload` 即可。

---


## 技术栈

Blink 1.3.12 · ArcartX 2.2.0.5 API · AuthMe 5.7.0 API · Kotlin 1.8.22 · Shadow 8.1.1

## License

GPL-3.0 —— 见 [LICENSE](LICENSE)。
