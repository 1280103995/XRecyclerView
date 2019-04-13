# XRecyclerView
<br>
&#160; &#160; &#160; &#160;在项目中经常用到RecyclerView，有时候业务需求，需要下拉刷新，上拉加载；有时候需要支持头部尾部视图、支持section等，为了完成业务需求，
引入了太多第三方库，而且大部分还不能满足需求，且这些库重复功能太多。在写这个库之前，也参考了一些第三方库，如：[XRecyclerView](https://github.com/XRecyclerView/XRecyclerView)、[FamiliarRecyclerView](https://github.com/iwgang/FamiliarRecyclerView)，
还有网络上的一些开源库。为了代码高度复用，减少布局层次，一两个通用适配器完成大部分功能，于是就有了这个库。<br>   

    该库特点： 
      1、一个RecyclerView支持下拉刷新，上拉加载，不需要嵌套其他布局。
      2、支持HeaderView、EmptyView、FooterView。且在EmptyView显示的时候，还能下拉刷新，以及是否显示HeaderView、FooterView。
      3、数据不满一屏时，自动加载更多数据，直至数据填充满屏幕，或者服务器没有更多数据了，调用setNoMore(true)。
      4、在刷新时不能加载更多数据，反之亦然。
      5、支持显示倒数第几个item时调用加载方法。
      6、为了项目内刷新view的统一，提供了一个XScrollView，只支持下拉刷新，不支持加载更多。
      7、不影响业务适配器RecyclerView.Adapter的定义。本库项目内提供了RVAdapter、MultiTypeRVAdapter，可供参考。
      
      
## **使用**
  
    implementation 'com.ganba:xrecyclerview:1.0.0'

## **提供的方法**

  * setRefreshView(IRefreshView view)   自定义一个下拉刷新view，需要实现IRefreshView接口
  * addHeaderView(View view)            添加一个头部视图
  * setEmptyView(View view)             设置一个空视图，在没有数据的时候显示
  * addFooterView(View view)添加一个尾部视图
  * setLoadMoreView(ILoadMoreView view)自定义一个上拉加载view，需要实现ILoadMoreView接口
  * setLimitNumberToCallLoadMore(int count) 设置显示倒数第几个item时调用加载更多方法，默认为1
  * setHeadFootWithEmptyEnabled(boolean enable) 设置显示空视图时是否显示头部尾部视图，默认不显示
  * refreshComplete() 刷新完成后调用，结束刷新
  * loadMoreComplete() 加载完成后调用，结束加载更多
  * setNoMore(boolean noMore) 设置没有更多数据时调用，调用之后不能再加载更多数据，下拉刷新后被重置
  * autoRefresh() 进入页面时自动刷新数据，需要在设置setLoadingListener后才会触发<br><br>
  更多方式参考源码
