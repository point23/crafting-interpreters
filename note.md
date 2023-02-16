# Part#1 总览

## Chap#01 介绍

#### Languages

DSL（Domain-Specific Language， "Little Languages"）

- Make, YMAL, XML, HTML, Batch, Emacs-Lisp, JSON
- ...

应用场景

- Documentation, Debugger, Editor Support, Syntax Highlighting

#### The First Interpreter

>  jlox in Java

自举（Bootstrapping）

>  You can use the compiled version of your own compiler to compile future versions of itself, and you can discard the original one compiled from the other compiler.

#### The Second Interpreter

> clox in C

C实现的语言

- Lua, CPython, Ruby's MRI...

计划功能

- 高级数据结构：动态数组、哈希表……
- 规划对象（Object）的内存结构
- 垃圾回收期（Garbage Collector）
- 将脚本转为字节码（Bytecode）

## Chap#02 路线图

![image-20230216130013266](note.assets/image-20230216130013266.png)

#### 源代码（Source Code）

​	纯文本

![image-20230216130130672](note.assets/image-20230216130130672.png)

### Section#1 Front End

#### 扫描（Scanning，也叫词法分析：lexing/lexing analysis）

​	词法分析器（Scanner/Lexer）以线性流的方式读取源代码中的字符，将他们切分为一个一个的词法单元（Token）。

![image-20230216132649704](note.assets/image-20230216132649704.png)

#### 解释（Parsing，也叫语法分析）

​	语法（Grammar）$G = (V_T, V_N, P, S)$

- Wikipedia: https://zh.wikipedia.org/zh/%E8%AF%AD%E6%B3%95

- 狭义：词如何组成短语和句子，即语法结构——这一部分严格意义称为句法（Syntax）
- 广义：对语法规则进行总结描述，或对语言使用的规范和限定，即语法规则

​	解释器（Parser）以词法单元序列作为输入，以树状结构的抽象语法树（Abstract Syntax Tree）作为输出。

​	解释器还承担了报告语法错误的任务（Syntax Error）。

![image-20230216135955073](note.assets/image-20230216135955073.png)

#### 静态分析（Static Analysis）

​	Step#1 绑定（Binding/Resolution）

​	找到语句中标识符（Identifier，Token的一种，如“a+b”中的a）的声明（Declaration，如a=10）处，将二者绑定（此处大概只是Name Binding），通识要注意绑定受作用域（Scope）限制，某个名称标识符只能绑定到该作用域下的一个声明。

- 对于静态类型（Statically Typed）的语言来说，绑定中需要发现类型错误。

​	Step#2 记录绑定结果

- 作为属性保存到AST的节点上
- 符号表：以标识符作为key
- xxx

### 	Section#2 Middle End 

#### 中间语言 (IR, Intermediate representation)

​	作为一个处在源语言和目标语言之间的接口，实现多源语言和多目标平台。

#### 优化（Optimization）

Wikipedia: https://en.wikipedia.org/wiki/Template:Compiler_optimizations

**O#1 常量叠算（Constant Folding）**

- 例

  ```c++
  // from 
  double a = 3.14159 * (0.75 / 2) * (0.75 /2);
  // to   
  double a = 0.4417860938;
  ```

- 在编译时计算常数计算并替代之

**O#2 常量传播（Constant Propagation ）**

- 例

  ```c++
  // Before
  int x = 14;
  int y = 7 - x / 2;
  return y * (28 / x + 2);
  // After
  int x = 14;
  int y = 0;
  return 0;
  ```

- 替代表示式中已知常数的过程，具体的传播路径由定义可达性（Reaching Definition）分析，重复多次常量传播+常量叠算的处理后，再进行死码清除（Dead Code Elimination）。

- 定义可达性

  ```c++
  // Case#1
  int x = 3; // d1
  int y = x; // d2
  //	d2 -----> d1
  // Case#2
  int x = 3;  // d1
  int x = 4;  // d2
  int y = x;  // d3
  //	d3 --X--> d1
  ```

  - 以表达式`p=a*b+c`为例，若1. 从初始节点到p处，计算过`a*b`且计算过后a和b没有被重新赋值，则认为该表达式在p点是可达的。

- 死码删除

  ```c++
  // Before:
  int a = 30;
  int b = 20;
  int c = 12;
  if (true) c = 2;
  return c * 2;
  // After:
  int c = 12;
  if (true) c = 2;
  return c * 2;
  ```

**O#03 公共子表达式删除(Common Subexpression Elimination)**

- 基于定义可达性，编译器将多个相同的表达式替换成一个变量

- 例

  ```c++
  // Before
  int a = b * c + g;
  int d = b * c + e;
  // After
  int temp = b * c;
  int a = temp + g;
  int d = temp + e;
  ```

**O#04 循环不变代码外提 (LICM, Loop-Invariant Code Motion, also know as hoisting or scalar promotion)**

- 将循环不变的语句或表达式移到循环体之外，而不改变程序的语义。

- 例

  ```c++
  // From:
  for (int i = 0; i < n; i++) {
      x = y + z;
      a[i] = 6 * i + x * x;
  }
  // After:
  x = y + z;
  t1 = x * x;
  for (int i = 0; i < n; i++) {
      a[i] = 6 * i + t1;
  }
  ```

**O#05 值编号(Value Numbering)**

- 例

  ```
  a := 3         a #1
  b := a         b #1
  c := a + b     c #2
  d := c         d #2
  ```

- 以全局(GVN)和局部的值编号进行区分

- 静态单赋值形式(SSA, Static Single Assignment Form)

  - 例

    ```
    // Normal
    y := 1
    y := 2
    x := y
    // SSA Form
    y1 := 1
    y2 := 2
    x  := y2
    ```

  - 中间语言的一种特性，每个变量仅被赋值一次， 许多编译器优化都是在使用SSA的基础上实现的

  - Wikipedia: https://en.wikipedia.org/wiki/Static_single-assignment_form

**O#06 强度折减(Strength Reduction)**

- 意在以等效但运算量小的计算替代昂贵的计算

- 例：

  - 用循环及加法取代乘法运算

    ```c++
    // Before
    c = 8;
    for (i = 0; i < N; i++)
    {
    	y[i] = c * i;
    }
    // After
    c = 8;
    k = 0;
    for (i = 0; i < N; i++)
    {
        y[i] = k;
        k = k + c;
    }
    ```

  - 逻辑位移运算子

    `y = x/8 ==> y = x >> 3`

**O#07 循环展开（Loop unrolling）**

- 牺牲程序的大小来加快程序执行速度

- 例

  ```c++
  for (int i = 1; i <= 60; i++)
  	a[i] = a[i] * b * c;
  
  for (int i = 1; i <= 58; i+= 3) {
  	a[i] = a[i] * b * c;
  	a[i + 1] = a[i + 1] * b * c;
  	a[i + 2] = a[i + 2] * b * c;
  }
  ```

### Section#3 Back End 

#### 代码生成(Code Generation)

设计决策：机器码还是字节码

- 机器码是对应平台/架构的原生代码，生成复杂但运行高速
- 字节码针对虚拟的CPU，指令通常一个字节大小，可移植性强但运行速度略慢

#### 虚拟机(Virtual Machine)

​	直观的思路是将源代码翻译为Bytecode后还需要编写“迷你编译器”来将字节码翻译到对应平台的机器码。

​	但我们也可以编写一个虚拟机，例如编写一个C语言的虚拟机，我们就可以在任何有C编译器的平台运行代码。

#### 运行时（Runtime）

​	为运行中的程序提供的服务，包内存管理（如使用GC，回收不再使用的内存）和运行类型信息（RTTI，使得部分对象实例在运行时才决定其具体类型）

### Section#4 捷径

#### 一次通过编译器(Single-Pass Compiler)

​	编译器的各个部分只通过一遍后就生成机器码

#### 语法树遍历解释器(Tree-Walk Interpreters)

​	在遍历语法树的过程中就对每个节点进行计算

#### 转译器(Transpilers)

​	从一种编程语言翻译到另一个等效源代码的编译器

​	将浏览器视为现代“机器”的话，JS则时它的“机器码”，因此也出现了许多转译到JS的转译器

#### 即时编译(JTT，Just-in-time Compilation)

​	JIT读取提前编译的字节码，动态地将他们编译为机器码

### Section#5 对比：编译器与解释器

编译：将源语言翻译为其他形式（不同层级，通常时更底层，如字节码和机器码）

解释：读取源语言然后直接执行

![image-20230216161339337](note.assets/image-20230216161339337.png)

# Part#2 语法树遍历解释器

## Chap#03 扫描（词法分析）

#### 词汇语法(Lexical Grammar)

​	即是描述如何由字符组合出词法单位(Lexeme)

- 【TODO】
  - 正规语言/乔姆斯基文法/有限状态机
  - 参考资料——the dragon book  

#### Exit Code

参考`<sysexit.h>`:  https://man.freebsd.org/cgi/man.cgi?query=sysexits&manpath=FreeBSD+4.3-RELEASE

- `EX_USAGE(64)`
  - The command is used incorrectly
- `EX_USAGE(65)`
  - The input data was incorrect in some way.





























