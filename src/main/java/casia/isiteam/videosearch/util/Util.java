package casia.isiteam.videosearch.util;

public class Util {

	/*
	 * 0-n 显示各层信息，0 显示本方法的信息，1显示调用此方法的地方
	 */
	public static void printContextInfo(String msg) {
		
		System.err.println(msg);
		StackTraceElement[] state = new Exception().getStackTrace();
		if (state.length == 1) {
			return;
		} else {
			System.err.format("ClassName:%s\n", state[1].getClassName());
			System.err.format("MethodName:%s\n", state[1].getMethodName());
			System.err.format("FileName:%s\n", state[1].getFileName());
			System.err.format("LineNumber:%s\n\n", state[1].getLineNumber());
		}
	}
}
