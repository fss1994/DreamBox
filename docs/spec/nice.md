## NICE
>试验性项目

### 背景

在`DreamBox`的开发过程中，我们遇到最多、需要投入精力最大的部分就是调和Android、iOS两个平台原生系统所提供的布局功能的对齐上。在Android平台我们使用约束布局，在iOS使用AutoLayout。两套布局算法背后的逻辑原理不尽相同，在有些使用场景和使用习惯上经常需要花大力气进行适配和取舍。

提出此项目是基于对Android约束布局代码的学习，评估认为如果将约束布局的源码移植成为C++代码，那么即可实现双端的布局逻辑算法完全一致的目的，节省双端SDK开发人员的适配工作。

### 内容

将约束布局的代码移植成为C++代码。以约束布局1.1.3版本代码为例，其自身设计非常优秀。首先通过ConstraintWidget映射原生View的各种属性，然后通过ConstraintAnchor来表示View的“边”依赖关系，最后通过LinearSystem线性系统对其进行计算。本质上，约束布局的算法就是一元多次方程式的解题过程。

通过阅读其代码，能感受到其在设计和书写上貌似也在考量作为C++代码的存在。

### 进度

当前已完成了ConstraintWidge和Anchor部分的代码移植工作，LinearSystem及其他周边辅助代码正在迁移中。

>后续如果有其他重大进展会在此文档中更新进度。