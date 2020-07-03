package lang;

/**
 * 消息接收
 */
public class MessageReceiver {
	private Object original;//原版信息
	private String name;//名字
	
	public MessageReceiver(Object object, String name) {
		this.original = object;
		this.name = name;
	}
	
	public Object getPlatformSender() {
		return original;
	}
	
	public <T> T getPlatformSender(Class<T> type) {
		return type.cast(original);
	}
	
	public String getName() {
		return name;
	}
}
