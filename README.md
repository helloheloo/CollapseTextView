# CollapseTextView
可展开或收缩的文本展示控件
##用法        
···java
<com.helloheloo.collapsetextview.CollapseTextView
      android:id="@+id/coll_tv"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      app:expandText="@string/expand_text"
      app:expandTextColor="@android:color/holo_purple"
      app:originalText="@string/original_text"
      app:originalTextColor="@android:color/holo_red_dark"
      android:layout_margin="30dp"
      />
···
app:expandText 需要展开或收缩的文字<br>
app:expandTextColor 需要展开或收缩的文字颜色<br>
app:originalText 默认显示的文字<br>
app:originalTextColor 默认显示的文字颜色<br>
