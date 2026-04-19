void main() { 
    System.out.println("Hello from instance: " + this.getClass().getSimpleName()); 
    System.out.println("1 + 2 = " + add(1, 2)); 
} 
int add(int a, int b) { 
    return a + b; 
}