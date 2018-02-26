<%@ page 
	language="java" 
	contentType="text/html; charset=UTF-8" 
	pageEncoding="UTF-8" 
	import="java.io.IOException, littleferry.share.*, littleferry.search.PostSearch" 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Little Ferry</title>
<link href="//cdn.bootcss.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet">
<script src="//cdn.bootcss.com/jquery/2.1.4/jquery.min.js"></script>
<script src="//cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
<body>
	<%! 
	private PostSearch search;
     int pageSize=2;
	 int pageCount;
	 int showPage;
	 int p;
	%>

	<%! 
	public void jspInit() {
		try {
			LittleFerryFileSystem.initialize();
			search = new PostSearch();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		//System.out.println("jspInit()");
	}
	
	public void jspDestroy() {
		//System.out.println("jspDestroy()");
	}
	%>
<div class="container" style="padding: 20px 20px 20px">
	<div   class="row">
		<div class="col-md-offset-4 col-md-4"  style="text-align:center">
			<image src="image/logo.jpg" width="170" height="38"/><br><br>
		</div>
	</div>
   <form class="bs-example bs-example-form" role="form" action="index.jsp#head" method="GET">
      <div class="row">
         <div class="col-md-offset-3 col-md-6">
            <div class="input-group">
               <input type="text" class="form-control" name="search-content">
               <span class="input-group-btn">
                  <button class="btn btn-default" type="submit">
                     搜索
                  </button>
               </span>
            </div><!-- /.input-group -->
         </div><!-- /.col-md-offset-3 .col-md-6 -->
      </div><!-- /.row -->
   </form>
   <div class="row">
		<div class="col-md-12">
			<hr id="head">
		</div>
	</div>
</div><!-- /.container -->

<% 
	String parameter = request.getParameter("search-content");
	if (parameter != null) {
		parameter=new String(parameter.getBytes("ISO-8859-1"),"UTF-8");
		//parameter = URLDecoder.decode(parameter,"UTF-8"); 
		//String candidate = search.singleSearch(parameter);
		LongPair[] pairs = search.search(parameter);
		if (pairs != null) {
			//String[] pairs = candidate.split(",");
			final int recordCount=pairs.length;
			pageCount=(recordCount%pageSize==0)?(recordCount/pageSize):(recordCount/pageSize+1);
			String integer=request.getParameter("showPage");
			if(integer==null){
				 integer="1";
			}
			try{showPage=Integer.parseInt(integer);
			}catch(NumberFormatException e){
			   showPage=1;
			}
			if(showPage<=1){
			   showPage=1;
			}
			if(showPage>=pageCount){
			   showPage=pageCount;
			}
			int start=(showPage-1)*pageSize;
			int end=start+pageSize;
			if(showPage==pageCount) end=pairs.length;
			for (int i=start;i<end;i++) {
				//String pair= pairs[i];
				LongPair pair = pairs[i];
				//final int index = pair.indexOf(":");
				//long frequency = Long.parseLong(pair.substring(index+1));
				long frequency = pair.getFirst();
				//BaiduPost post = search.getBaiduPost(Long.parseLong(pair.substring(0, index)));
				BaiduPost post = search.getBaiduPost(pair.getSecond());
				if (post != null) {
%>
<div class="container">
	<div class="row">
		<div class="col-md-offset-2 col-md-8">
			<table class="table table-hover">
   				<tbody>
      				<tr>
      					<%p=0; %>
         				<td class="col-md-2">标题</td>
         				<td><%=post.getTitle() %></td>
      				</tr>
      				<tr>
        			 	<td class="col-md-2">贴吧</td>
        			 	<td><%=post.getBar() %></td>
     			 	</tr>
     			 	<tr>
        			 	<td class="col-md-2">回复数</td>
        				<td><%=post.getReplies() %></td>
      				</tr>
      				<tr>
        			 	<td class="col-md-2">链接</td>
        				 <td><a href=<%=post.getUrl()%> target="_blank"><%=post.getUrl() %></a></td>
      				</tr>
      				<tr>
         				<td class="col-md-2">关联度</td>
         				<td><%=frequency %></td>
      				</tr>
   				<tr>
          				<td class="col-md-2"><a data-toggle="collapse" data-parent="#accordion"  href="#collapse<%=i %>">帖子内容</a></td>
          				<td>
          					<div id="collapse<%=i %>" class="panel-collapse collapse">
    							<table class="table table-hover">
    								<tbody>
    								<%for(BaiduNote note:post.getNotes()){%>
      									<tr>
        			 						<td class="col-md-2">作者</td>
        									<td><%=note.getAuthor()%></td>
      									</tr>
      									<tr>
        			 						<td class="col-md-2">正文</td>
        									<td><%=note.getText() %></td>
      									</tr>
      									<% }%>
      								</tbody>
      							</table>
  							</div>
          				</td>
          			</tr>	
   				</tbody>
			</table>
		</div><!-- /.col-md-offset-2 .col-md-8 -->
	</div><!-- /.row -->
</div><!-- /.container -->
<%
				}
			}
		} else {
%>
<div class="container">
	<div class="row">
		<div class="col-md-offset-2 col-md-8">
			<div class="alert alert-dismissable fade in alert-warning">
				<button contenteditable="false" type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
				<strong>提醒：</strong>没有结果
				<%p=1; %>
			</div><!-- /.alert .alert-dismissable .fade .in .alert-warning -->
		</div><!-- /.col-md-offset-2 .col-md-8 -->
	</div><!-- /.row -->
</div><!-- /.container -->
<%
		}
	}
%>
<br>
 <%if(parameter!=null && p==0){ %>
 <center>
 	<ul class="pagination">
 		<li><a href="index.jsp?search-content=<%= parameter%>&showPage=1#head">首页</a></li>
  		<li><a href="index.jsp?search-content=<%= parameter%>&showPage=<%=showPage-1%>#head">上一页</a></li>
		<% //根据pageCount的值显示每一页的数字并附加上相应的超链接
			int start= showPage-3;
			int end = showPage+3;
			if(start<1) start=1;
			if(end>pageCount) end = pageCount;
			for(int i=start;i<=end;i++){
	  			String c;
	 			if(i==showPage) c="active";
	 			else c="";
 		%>
   		 <li class=<%= c%>><a href="index.jsp?search-content=<%= parameter%>&showPage=<%=i%>#head"><%=i%></a></li>
		<% }%> 
  		<li><a href="index.jsp?search-content=<%= parameter%>&showPage=<%=showPage+1%>#head">下一页</a></li>
  		<li><a href="index.jsp?search-content=<%= parameter%>&showPage=<%=pageCount%>#head">末页(<%=pageCount %>)</a></li>
 	</ul>
  </center>
  <%} %>
	
</body>
</html>