# 使用FTP连接池封装工具类
## 背景
早前的思路是将FTP连接的管理（对象池功能）与FTP工具类（上传下载方法）在一个工程中实现，在工具类中调用是模板类提供的模板方法，
由模板方法与对象池打交道——初始时获取对象，结束时归还对象。将对象池引用在模板类中暴露出来，模板类的模板方法需要很多的样板式代码，
虽然这是不可避免的，但是模板方法即API的设计限制了扩展的可能。
为了不暴露对象池引用，在[ftpcp](https://github.com/Honwhy/ftpcp)项目中已经将对象池功能封装到内部，自然地使用获取资源和关闭资源方式，这在新的模板方法中可以节省不少笔墨。
另外参考Spring的JdbcTemplate的实现，或许可以提供一种新的思路去设计模板类。

## 改造
在设计工具类之前先设计模板类，这回模板类的模板方法不执行具体的操作逻辑了，把这些逻辑交给回调类。而工具类是建立在模板类基础上的，更多地是提供回调接口的具体实现来完成整体的操作。
### 设计模板类
首先定义通用的操作接口方法，使用的是泛型方法，
```
public interface FTPOperations {
	
	<T> T execute(FTPClientCallback<T> action) throws FTPAccessException;
	
	<T> T execute(FTPConnectionCallback<T> action) throws FTPAccessException;
	
	<T> List<T> execute(ListFTPConnectionCallback<T> action) throws FTPAccessException;
}
```
然后是继承实现模板类，
```
public class FTPClientTemplate implements FTPOperations, InitializingBean{
  private FTPManager ftpManager;
	public <T> T execute(FTPConnectionCallback<T> action) throws FTPAccessException {
		Assert.notNull(action, "Callback object must not be null");

		FTPConnection conn = null;
		try {
			conn = FTPConnectionUtil.getConnection(ftpManager);
			return action.doInFTPConnection(conn);
		}
		catch (FTPException ex) {
			throw new FTPAccessException(ex);
		}
		finally {
			FTPConnectionUtil.releaseConnection(conn);
		}
	}
}
```
模板类中引入`FTPManager`，可以从中获取连接对象。模板方法主要的逻辑是获取连接对象，然后将连接对象交给回调对象，由回调对象执行具体的逻辑，最后将连接对象释放。
这里完全看不到连接池的影子。
```
public interface FTPConnectionCallback<T> {

	T doInFTPConnection(FTPConnection conn) throws FTPException, FTPAccessException;
}
```
模板方法是泛型方法，而回调接口的泛型是定义在接口上的，在这里可以这样理解，由回调接口的类型推导模板方法的返回类型，具体的返回类型还是由具体的执行逻辑来决定。
回调并不是异步，回调是具体操作逻辑的封装。
### 设计工具类
在工具类中引入模板类，构造回调实例，调用模板方法即可，比如上传文件方法中，
```
public class CommonFAOSupport implements InitializingBean {
	public boolean uploadFile(final String directory, final String fileName, final String content) {
		FTPConnectionCallback<Boolean> action = new FTPConnectionCallback<Boolean>() {

			public Boolean doInFTPConnection(FTPConnection conn) throws FTPException, FTPAccessException {
				FTPClient client = conn.unwrap(FTPClient.class);
				boolean flag = false;
				BufferedInputStream bis = null;
				try {
					flag = client.changeWorkingDirectory(directory);
					if(flag) {
						bis = new BufferedInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8.name())));
						flag = client.storeFile(fileName, bis);
					}
					return flag;
				} catch (IOException e) {
					throw new FTPAccessException(e);
				}
			}
			
		};
		return ftpClientTemplate.execute(action);
	}
}
```
在工具类的方法内写好执行逻辑封装成回调实例，传递给模板方法`ftpClientTemplate.execute(action)`，获取连接对象和释放都由模板方法来完成，
回调实例中假定一定能获得连接对象，直接使用。
工具类中还有多线程下载的例子，略微有点负责，一次使用多个连接对象，不过获取多个连接对象也是由模板方法来完成的，职责上还是清晰的。
总体上看，回调对象获得连接对象后就可以做任何事情了，而模板方法再也没有具体的执行逻辑了。
## 项目地址
https://github.com/Honwhy/commons-pool-ftp 见ftpcp分支