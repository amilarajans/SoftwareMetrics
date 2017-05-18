#Readme

- javac *.java
- java Assignment2 /home/user/desktop/ A.class method1()
- java Assignment2 /home/user/desktop/ A.class


it'll show the following out put

```
A.method1()
	B constructor(long)
	Object constructor()
	B.method1()
	B.method2()
		C constructor()
		Object constructor()
		C.method1()
		C.method2()
A.method2(int)
A.method2(int, double)
A.method3(String)

Total number of classes involved    ---- 3
Total number of unique constructors ---- 2
Total number of unique methods      ---- 8

```