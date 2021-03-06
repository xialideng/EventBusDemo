# EventBus总结

## 简介
> 	当前版本：3.0  
> 	官网：http://greenrobot.org/  
> 	说明：下列文本性内容均翻译自官网最新的开发文档  
> 	版权声明：本文为原创文章，未经允许不得转载  
> 	博客地址：http://blog.csdn.net/kevindgk  
> 	GitHub地址：https://github.com/KevinDGK/EventBusDemo

## Features	
	1.简单且强大：EventBus是一个非常容易学会的轻量级API类库。你的软件架构可以通过组件间的结构而得到好处：当使用事件(event)的时候，订阅者不需要知道关于发送者的信息。  
	2.经历了实战测试：EventBus是使用最多的Android库之一；  
	3.高性能：特别是在Android中，性能十分重要。EventBus剖析和充分利用了很多，很可能会成为最快的解决方案；  
	4.基于API的方便快捷的注解：仅仅需要将@Subscribe注解修饰订阅的方法即可。  
	5.主线程投递：当需要UI交互的时候，EventBus能够将时间投递到主线程执行，而不用管事件是怎么投递到主线程中的。  
	6.子线程投递：当你的订阅者做耗时操作的时候，EventBus也能够开子线程执行任务，防止UI线程阻塞。  
	7.零配置：在代码中任何位置使用默认的EventBus，不用做任何过多的配置。  
	8.事件和订阅者都可以继承。
	9.可配置的：为了使得EventBus满足你的需求，你可以使用builder部分来调整它的行为 。  

## How to get started

### 添加EventBus依赖到Gradle中
	compile 'org.greenrobot:eventbus:3.0.0'  

### 1.定义事件  
	Event(事件)使用POJO(简单的javaBean)。  
	public class MessageEvent {
    	public final String message;

    	public MessageEvent(String message) {
        	this.message = message;
    	}
	} 

### 2.准备订阅者  
	2.1 订阅者实现事件的处理方法，也叫"subscriber methods"，当事件event被投递的时候就会调用该方法。
	该方法必须被 @Subscribe 注解定义。  
	请注意，EventBus3的方法名称可以被任意选择，不需要向EventBus2中那样约定的命名方式。

	// This method will be called when a MessageEvent is posted
	@Subscribe
	public void onMessageEvent(MessageEvent event){
	    Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
	}
	
	// This method will be called when a SomeOtherEvent is posted
	@Subscribe
	public void handleSomethingElse(SomeOtherEvent event){
	    doSomethingWith(event);
	}  

	2.2 订阅者也需要注册和取消注册，仅仅当订阅者被注册的时候，才能接收到事件。  
	在Android中,Activity和Fragments通常根据生命周期绑定EventBus。   
	
	- 在onCreate和onDestroy方法中注册和取消注册 (建议)  
	- 在onStart和onStop方法中注册和取消注册	 (分屏的时候可以使用)
	根据需要，一般可以选择第一种，第二种界面不可见后就取消了注册。

	@Override
	public void onStart() {
	    super.onStart();
	    EventBus.getDefault().register(this);
	}
	
	@Override
	public void onStop() {
	   EventBus.getDefault().unregister(this);
	    super.onStop();
	} 


### 3.投递事件  
	在代码的任何位置都可以投递事件，所有的当前注册的并且匹配事件类型的订阅者都会接收该事件。  
	EventBus.getDefault().post(new MessageEvent("Helloeveryone!"));  

----------

## Delivery Threads (ThreadMode)
	EventBus能够帮你操作线程：事件能够被投递到和发送的进程不相同的其他线程中。最常见的用法就是用于修改UI界面。在Android中，  
	修改UI界面必须在UI线程中进行。另一方面，访问网络或者一些耗时的任务，不能够在主线程中执行。EventBus帮助你来处理这些任务，  
	并且和UI线程同步执行，而不需要必须考虑线程的转场或者使用AsyncTask等等。  

	在EventBus中，你可以通过使用下列四种线程模式来定义处理方法在哪个线程中执行。  

### ThreadMode: POSTING  
	订阅者会在投递的线程中被调用，这个是默认的模式。事件投递是同步执行的，一旦投递完毕，所有的订阅者都会被调用。

	@Subscribe(threadMode = ThreadMode.POSTING) // ThreadMode is  optional here 
	public void onMessage(MessageEvent event) {  
	    log(event.message);  
	} 
### ThreadMode: MAIN  
	订阅方法会在UI线程中执行。如果投递的方法来自主线程，那么该方法立刻在主线程中执行。如果投递的方法来自子线程，那么该方法立刻  
	在主线程中执行。所以，该方法通常用于子线程想要修改UI界面的时候，可以通过这种方式。但是应该保证执行的方法不能包含耗时操作，  
	防止阻塞主线程。
	
	// Called in Android UI's main thread
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessage(MessageEvent event) {
	    textField.setText(event.message);
	}
### ThreadMode: BACKGROUND  
	订阅方法会在后台线程中被调用，如果发送的线程是子线程，那么方法会直接在这个线程中执行；如果发送的线程是主线程， 
	那么EventBus会开启一个单独的子线程去执行这个方法，但是也需要注意避免阻塞子线程。
	// Called in the background thread
	@Subscribe(threadMode = ThreadMode.BACKGROUND)
	public void onMessage(MessageEvent event){
	    saveToDisk(event.message);
	}
### ThreadMode: ASYNC
	订阅方法会在单独的异步线程中执行，总是和发送的线程不同。事件处理方法如果是耗时操作，比如网络访问，就需要使用这种方式。  
	避免同时触发大量的长时间运行的异步线程去执行订阅方法，来限制线程的并发量。EventBus本身会使用管理一个高效线程池，  
	当接收到异步线程的任务执行完毕的通知后，将该线程回收，等待重复利用。   
	
	// Called in a separate thread
	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(MessageEvent event){
	    backend.send(event.message);
	}

## Demo示例
	
	![](http://i.imgur.com/PG8bAMB.gif)

----------

## Configuration  配置  
	EventBusBuilder配置EventBus各种相关的设置。例如，下面的方法就是如何当一个投递的事件没有订阅者的时候，让EventBus保持沉默。

	EventBus eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).build();

	另外一个配置是失败时抛出异常，但是这个异常不用做任何处理。  
	EventBus eventBus = EventBus.builder().throwSubscriberException(true).build();
	
	PS：我测试了一下，没什么卵用...

## 配置默认的EventBus实例  
	EventBus.getDefault()是一种简单的方式，可以在app任何位置来获取一个共享的EventBus实例。  
	EventBusBuilder也允许使用installDefaultEventBus()方法来配置默认的EventBus实例。  
	例如：我们需要EventBus实例再次抛出异常，可以调用下列方法：
	EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();  
	
	但是，会有两个限制：
	1.必须在第一次获取默认实例之前调用这个方法，确保和app行为一致，比如可以在Application厘米那配置；
	2.有可能会造成程序崩溃；

----------
## Sticky Events 粘性事件
	当一个事件被post之后，也许我们对它携带的信息仍然很感兴趣。比如说。一个事件，标志着初始化完成。或者是你有一些传感器或者定位的信息，并且你想保持住他们最新的数值。  
	这个时候，你就可以直接使用粘性事件(sticky events)，而不用自己做缓存。EventBus在内存中以一种特定的格式保存最新的数据，并且这个粘性事件能够被发送给订阅者  
	或者明确查询。如此，你不许要任何特定的逻辑去考虑获取已经存在的数据。  
	
	1.发布事件
	EventBus.getDefault().postSticky(new MessageEvent("Hello everyone!"));  
	
	2.接收事件
	当该事件被post之后，如果一个Activity启动了以后，这个事件就会立刻被发送给它(如果它内部包含订阅者)。在注册时期，所有的粘性订阅方法都会被立刻得到之前post的粘性事件。

	@Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent event) {
        // UI updates must run on MainThread
        textField.setText(event.message);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

	3.手动获取或移除粘性事件
	就像上面介绍的那样，最新的粘性事件当activity/fragment注册的时候，就会自动匹配投递订阅者。但是有时候，也可以非常方便的手动查询或者删除粘性事件。因为只有移除了粘性事件之后，才不会被发送。
	
	查询：
	MessageEvent stickyEvent = EventBus.getDefault().getStickyEvent(MessageEvent.class);
	删除：
	// Better check that an event was actually posted before
	if(stickyEvent != null) {
	    // "Consume" the sticky event
	    EventBus.getDefault().removeStickyEvent(stickyEvent);
	    // Now do something with it
	}  
   
	总结：  
	1.可以用来保存一些数据，比如有多个地方(页面)需要使用的数据：传感器信息、登陆信息等；  
	2.如果需要更新数据，仅仅需要post/get stikcy事件即可，不需要有任何的保存和读取数据的逻辑；  
	3.如果不需要，可以手动删除(单个或所有) 
----------
## Priorities and Event Cancellation 优先级和事件取消

----------

## Subscriber Index	订阅者索引
	EventBus 3.0新特性，这是一个可选择的优化来加速订阅者注册初始化。订阅者索引可以在项目构建(build)的时候，使用EventBus注解处理器来创建的。  
	虽然不是必须使用索引，但是在Android应用的时候仍然推荐使用索引来提升性能。
	
----------
## 源码分析
### EventBus.getDefault()
	EventBus.getDefault()获取的是单例，并且双重判断，防止并发：
	/** Convenience singleton for apps using a process-wide EventBus instance. */
    public static EventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }
## ProGuard 混淆

----------	
## EventBus和广播的区别

----------	
## EventBus和RxJava(RxAndroid)区分

----------
## 注意事项
1. 如果修改EventBus的相关配置，或者删除了一些事件，那么调试运行程序的时候，需要clean一下，否则上一次编译时EventBus注册的配置和事件还存在。  