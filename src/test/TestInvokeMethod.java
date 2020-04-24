package test;

public class TestInvokeMethod {

	public static void main(String[] args) {
		
		try {
			var method = TestInvokeMethod.class.getDeclaredMethod("test", String.class, int.class);
			method.invoke(new TestInvokeMethod(), "arg0", 1);
		}catch(Exception e) { }
	}
	
	public void test(String s, int i) {
		System.out.printf("passed %s and %d\n", s, i);
	}
}
