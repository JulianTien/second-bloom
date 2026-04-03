# Loop 项目现状审计报告

更新日期：2026-03-22

## 1. 项目概览与审计范围

本报告基于根目录 [PRD.md](/Users/peng/AndroidStudioProjects/Loop/PRD.md) 与当前仓库中的现有代码、配置和测试文件，对 Loop 项目的需求理解和开发现状进行一次静态审计。目标是形成截至当前时间点的项目快照，帮助后续开发明确“已经具备什么、还缺什么、风险在哪里”。

本次审计覆盖以下内容：

- 需求文档：`PRD.md`
- 构建配置：`build.gradle.kts`、`settings.gradle.kts`、`gradle/libs.versions.toml`、`app/build.gradle.kts`
- Android 配置：`AndroidManifest.xml`、`res/values*`、`res/xml`
- Kotlin 源码：`app/src/main/java/com/scf/loop/**`
- 测试代码：`app/src/test/**`、`app/src/androidTest/**`

本次审计不包含：

- `build/`、`.gradle/` 等生成产物
- 任何功能开发、重构、性能优化或 UI 改造
- 对后端、AI 服务、图像模型或第三方接口的臆测性实现判断

状态判定口径如下：

- 已完成：代码中存在可运行实现，且已接入主流程
- 部分完成：仅有页面占位、静态文案、状态骨架或导航入口
- 待实现：PRD 明确提出，但代码、依赖或配置中无对应实现

## 2. PRD 核心需求拆解

### 2.1 功能需求

根据 [PRD.md](/Users/peng/AndroidStudioProjects/Loop/PRD.md)，项目的核心功能链路应包括：

| 需求维度 | PRD 要求 | 当前状态 |
|---|---|---|
| 上传识别 | 上传照片、支持多图、格式/大小校验、上传反馈、AI 识别、识别结果可编辑 | 待实现 |
| 改制方案生成 | 选择改制类型、AI 生成多方案、难度等级、材料清单、预计耗时 | 待实现 |
| 效果预览确认 | 前后对比、360°预览、放大查看、风格调整、重新生成、保存/分享/导出/预约 | 待实现 |
| 记录管理 | 查看历史改制记录、管理个人成果 | 部分完成 |
| 边缘场景处理 | 杂乱背景检测、自动抠图、用户辅助标注、背景替换、异常处理矩阵 | 待实现 |
| 社会价值表达 | 将可持续时尚、SDG 对齐、教育意义体现在产品定位中 | 部分完成 |

### 2.2 非功能需求

PRD 虽以产品流程为主，但已隐含多项非功能要求：

- 交互反馈完整：要求即时反馈、处理中反馈、完成反馈、异常反馈四层机制
- 可用性较高：复杂背景、遮挡、模糊、非衣物等场景都要求给出清晰提示
- 可扩展性要求高：预留 AR 试穿、社区分享、预约裁缝等能力
- 视觉一致性：强调环保主题、结果展示、进度反馈与可视化表达
- 可信度要求：AI 识别后必须允许用户确认和修正
- 业务闭环完整：从上传到识别、改制、确认、保存、分享形成完整链路

当前代码仅覆盖视觉骨架与少量无障碍语义，尚未落实上述大部分非功能能力。

### 2.3 用户故事

PRD 中可提炼出以下关键用户故事：

- 作为普通用户，我希望上传旧衣照片，让系统识别衣物类型、颜色、材质和瑕疵
- 作为想改造旧衣的用户，我希望选择改制目的，并获得 AI 生成的多种改制方案
- 作为审阅方案的用户，我希望看到直观预览，并能调整参数、重新生成或确认保存
- 作为在复杂背景下拍照的用户，我希望系统能自动处理背景，必要时引导我手动框选
- 作为持续使用平台的用户，我希望保存历史记录、分享成果并获得持续激励
- 作为认同可持续理念的用户，我希望产品传递环保、教育和社会价值

当前实现仅对“进入应用后看到上传页、改制页、记录页入口”提供了非常初步的支撑，距离真实用户故事落地仍有明显差距。

### 2.4 业务流程

PRD 定义的标准业务流程为：

1. 用户上传衣物照片
2. 系统完成图像质量评估与背景复杂度判断
3. 若背景复杂，则执行自动预处理或用户辅助标注
4. 进入衣物识别流程，生成可编辑识别结果
5. 用户选择改制类型
6. 系统生成改制方案和效果预览
7. 用户调整方案、确认结果
8. 系统支持保存、分享、导出和后续服务衔接

当前代码中的实际流程为：

1. 启动 App
2. 进入底部导航三页面骨架
3. 分别看到“上传”“改制”“记录”三个静态占位页面

结论：PRD 所要求的业务流程尚未实现，当前仅具备流程入口级的页面导航骨架。

## 3. 已完成功能清单

以下能力已在代码中实现并可视为“已完成”：

- 单模块 Android 应用工程已初始化完成，项目名为 `Loop`
- 使用 Kotlin + Jetpack Compose + Material 3 构建应用 UI
- `MainActivity` 已作为应用入口完成启动与主题挂载
- 应用已具备底部导航主框架，包含“上传”“改制”“记录”三个页面入口
- 导航使用 Navigation Compose 实现，支持页面切换与基础动画过渡
- 已完成品牌化主题基础建设，包括浅色/深色主题资源与 Compose 主题令牌
- 页面与导航项已添加部分无障碍语义描述
- 示例单元测试和示例仪器测试存在且可通过
- 通过 `bash gradlew :app:testDebugUnitTest` 验证，单元测试构建成功
- 通过 `bash gradlew :app:assembleDebug` 验证，Debug 包可成功构建

可归入“工程基础已搭好”的具体内容包括：

- 根级 Gradle 配置与版本目录管理
- `app` 模块 Compose 构建开关与依赖配置
- Android 主题、图标、备份规则与 Manifest 基础配置
- 三个主页面的静态文案与视觉占位结构

## 4. 待实现功能清单

### 4.1 部分完成

以下项已有骨架，但未形成真实业务能力：

- 上传页：存在 [HomeScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/screens/HomeScreen.kt) 静态入口文案，但无拍照、相册、上传、校验与反馈逻辑
- 改制页：存在 [WorkbenchScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/screens/WorkbenchScreen.kt) 静态说明和 `WorkbenchState` 占位状态模型，但无工作流控制、AI 调用、方案渲染或用户交互
- 记录页：存在 [ProfileScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/screens/ProfileScreen.kt) 静态文案，但没有任何历史记录数据来源、列表或详情
- 社会价值表达：环保主题与产品定位体现在命名和配色上，但未形成独立的产品内容层或数据指标展示

### 4.2 待实现

依据 PRD，以下核心能力当前均未实现：

- 相机拍照与相册选图接入
- 多图上传能力
- 文件格式、大小、清晰度等校验
- 上传进度、识别动画、等待时间和异常反馈
- AI 衣物识别
- 颜色、材质、款式、瑕疵检测与结果修正
- 改制目标选择与多方案生成
- 方案效果图、前后对比图、预览缩略图
- 难度等级、所需材料、预计耗时展示
- 颜色、款式、元素等参数调整
- 局部重绘、重新生成和冲突处理
- 收藏、保存、Toast 反馈、分享社区、导出指南
- 预约裁缝服务
- 360°查看、细节放大、AR 试穿、场景模拟
- 杂乱背景检测与复杂度判断
- AI 自动抠图与分割验证
- 用户辅助框选与渐进式引导流程
- 多件衣物、遮挡、过暗过亮、模糊、非衣物等异常场景处理
- 历史记录持久化
- 用户账户、收藏、社区等数据管理
- 网络层、服务层、数据层、图片处理链路
- ViewModel、Repository、UseCase、依赖注入和统一状态管理
- 埋点、日志、监控、性能和安全策略
- 面向真实业务的测试体系

## 5. 技术栈说明

当前项目已确认使用的技术栈如下：

| 类别 | 当前技术 |
|---|---|
| 平台 | Android |
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose |
| 设计体系 | Material 3 |
| 导航 | Navigation Compose |
| 构建 | Gradle Kotlin DSL |
| 最低系统版本 | minSdk 24 |
| 目标系统版本 | targetSdk 35 |
| 编译版本 | compileSdk 35 |
| Java/Kotlin 目标 | Java 11 / JVM 11 |
| 测试 | JUnit4、AndroidX Test、Espresso、Compose UI Test 依赖 |

从依赖层面看，当前项目尚未接入以下任何业务级基础设施：

- 网络请求框架
- JSON 序列化框架
- 图片加载或图片处理库
- 本地数据库或键值存储方案
- 依赖注入框架
- 协程相关业务层封装
- AI 服务 SDK 或第三方图像模型接入

因此当前技术栈适合支撑原型 UI，但尚不足以支撑 PRD 对应的完整产品能力。

## 6. 代码结构与架构分析

### 6.1 目录与模块结构

当前仓库仅包含一个 Android 应用模块：

- 根项目：构建脚本、版本目录、PRD 文档
- `app` 模块：Android 应用代码与资源

源码主要集中在以下路径：

- [MainActivity.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/MainActivity.kt)
- [MainScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/MainScreen.kt)
- [WorkbenchScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/screens/WorkbenchScreen.kt)

此外还包含：

- `navigation/Screen.kt`：底部导航枚举定义
- `ui/screens/*`：三个主页面
- `ui/components/LoopBottomNavBar.kt`：底部导航组件
- `ui/theme/*`：主题与颜色体系

### 6.2 架构判断

当前项目架构本质上是：

- `Activity + Compose + Navigation Compose` 的 UI 壳层结构

当前没有观察到明确的分层架构设计，未发现以下常见结构：

- `data/domain/presentation` 分包
- Repository
- UseCase
- ViewModel
- 依赖注入容器
- 网络客户端
- 本地数据库
- 图片上传或图像处理管线

换言之，当前工程更接近一个“页面骨架原型”，而不是已进入业务开发阶段的完整应用架构。

### 6.3 代码量与实现深度

当前 `app/src/main/java` 下 Kotlin 源码总量约为 839 行，主要分布为：

- 页面与导航骨架
- 品牌主题和颜色定义
- 少量状态占位

这说明当前开发重点仍停留在：

- 工程初始化
- 基础 UI 搭建
- 品牌视觉基线

尚未进入：

- 业务流程建模
- 数据建模
- 服务接入
- 异常处理
- 真实状态驱动 UI

### 6.4 各文件角色分析

主要文件职责如下：

- [MainActivity.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/MainActivity.kt)：应用启动入口，负责挂载主题与主界面
- [MainScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/MainScreen.kt)：搭建 `Scaffold`、底部导航与三页面 `NavHost`
- [Screen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/navigation/Screen.kt)：定义三个导航目标及其图标、标题和语义
- [HomeScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/screens/HomeScreen.kt)：上传页占位
- [WorkbenchScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/screens/WorkbenchScreen.kt)：改制页占位及简单状态结构
- [ProfileScreen.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/screens/ProfileScreen.kt)：记录页占位
- [LoopBottomNavBar.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/components/LoopBottomNavBar.kt)：导航组件及选中态动画
- [Theme.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/theme/Theme.kt) 与 [Color.kt](/Users/peng/AndroidStudioProjects/Loop/app/src/main/java/com/scf/loop/ui/theme/Color.kt)：主题和配色体系

结论：代码结构清晰但非常浅，适合后续扩展，但现阶段还不能支撑 PRD 业务闭环。

## 7. 潜在技术风险

### 7.1 需求与实现差距风险

PRD 描述的是一款 AI 驱动、包含图像预处理、识别、生成、预览、分享与记录闭环的完整产品，但当前实现仅为三页静态导航原型，需求与实现存在显著断层，交付风险高。

### 7.2 基础设施缺失风险

当前没有网络层、数据层、AI 接口层、图片处理链路、持久化方案或状态管理体系。后续若直接叠加功能，容易造成架构失控、页面逻辑耦合和返工成本上升。

### 7.3 图像与 AI 能力集成风险

PRD 对复杂背景处理、衣物识别、改制生成、预览渲染提出了较高要求，但仓库中不存在相关依赖、算法模块或接口抽象。后续接入第三方 AI 服务时，接口设计、性能、费用和时延都可能成为关键风险。

### 7.4 工程规范风险

- `gradlew` 当前缺少执行权限，需要通过 `bash gradlew` 运行，说明本地工程规范性仍有欠缺
- 当前目录不是完整 Git 仓库，缺少正常版本追踪上下文，不利于持续审计和变更回溯

### 7.5 测试覆盖风险

当前测试仍是模板示例：

- 单元测试仅验证 `2 + 2 = 4`
- 仪器测试仅验证应用包名

这意味着当前几乎没有：

- UI 行为测试
- 导航测试
- 状态流测试
- 业务流程测试
- 回归测试

一旦开始快速叠加业务功能，缺乏自动化保障将显著提高回归风险。

## 8. 当前开发进度评估

### 8.1 分项评估

| 维度 | 评估 |
|---|---|
| 工程初始化 | 已基本完成 |
| 视觉主题与品牌基线 | 已基本完成 |
| 页面导航骨架 | 已基本完成 |
| 上传识别主流程 | 基本未开始 |
| AI 改制生成 | 基本未开始 |
| 预览确认闭环 | 基本未开始 |
| 复杂背景处理 | 未开始 |
| 数据层与服务层 | 未开始 |
| 持久化与记录管理 | 基本未开始 |
| 测试体系建设 | 未开始 |

### 8.2 综合判断

从“PRD 全量范围”而非“当前已有页面数”来评估，项目当前更准确的定位是：

- 处于原型/脚手架阶段
- 工程基础与视觉骨架完成度较高
- 业务主链路接近未开始

综合进度建议评估为：

- 约 15% 左右

该数值属于工程化估算，依据是：

- 基础壳层已可运行并可打包
- 但 PRD 中最关键的业务链路、AI 能力、数据基础设施与异常处理体系尚未落地

## 9. 结论与后续优先级建议

### 9.1 结论

Loop 当前并不是“功能开发到中后期”的项目，而是“需求目标很大、工程骨架刚搭好”的早期原型。现有实现已经完成品牌主题、三页导航和可运行工程的搭建，但尚未进入 PRD 所定义的核心产品能力实现阶段。

如果继续推进，后续工作重点不应放在局部 UI 微调，而应优先补齐业务架构和主链路能力，否则很难支撑 AI 改制平台的实际交付目标。

### 9.2 后续优先级建议

建议后续开发按以下优先级推进：

1. 先补齐应用架构基础
2. 再落地最小可行主链路
3. 最后扩展复杂场景与增强能力

更具体地说，建议优先推进：

- 建立 `presentation/domain/data` 或等效分层
- 引入 ViewModel 与统一 UI 状态管理
- 打通图片选择与上传入口
- 设计识别结果与改制方案的数据模型
- 抽象 AI 服务接口，先实现可替换的 mock 流程
- 实现“上传 -> 识别 -> 方案 -> 确认”最小闭环
- 在最小闭环稳定后，再引入复杂背景处理、记录持久化、分享与社区能力

以上建议仅用于后续研发排期参考，不代表这些能力已经在当前代码中具备。
