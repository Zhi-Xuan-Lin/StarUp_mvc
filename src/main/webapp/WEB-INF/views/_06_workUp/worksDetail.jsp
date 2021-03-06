
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:include page="/fragment/header.jsp" />
	<div style="display:flex">     
		<p style="color:white; font-weight:300; font-size:26px; flex-grow:2; align-self:flex-end; margin-left:30px;">${works.worksName}</p>  
	   	<p style="color:white; font-weight:200; line-height:60px; margin-right:10px">by ${works.author}</p>		
	    <img class="rounded-circle" height='60px' width='60px' 						 
						src="getUserPicture/${works.user_Id}" onclick="location.href='personalPageReadOnly?id=${works.user_Id}'"> 
	</div>      
	<div class="container-fluid lightbox-gallery" style="display:flex;">		     	       	
  		<div class="item" id="item1" style="margin:10px">                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
  			<img id="img1" src="mainWorksPicture/${works.works_id}" style="height:50vh">  
  			<p id="intro1" style="color:white; font-weight:200; margin-top:5px">${works.worksIntro}</p>
  		</div>
  		<div class="item" id="item2" style="margin:10px">
  			<img id="img2" src="readCaptionImg_1/${works.works_id}" style="height:50vh">
  			<p id="intro2" style="color:white; font-weight:200; margin-top:5px">${works.detail_1}</p>
  		</div>  
  		<div class="item" id="item3" style="margin:10px">
  			<img id="img3" src="readCaptionImg_2/${works.works_id}" style="height:50vh"> 
  			<p id="intro3" style="color:white; font-weight:200; margin-top:5px">${works.detail_2}</p>
  		</div>
  	 
	</div>	

<!-- =================留言區================== -->
<hr>
<div class="container" style="margin:0px 20px;">

	<div class="form-group">
		<form name="uppdateComment" id="uppdateComment" method="POST">
			<div  style="display:flex">
				<div>
					<textarea class="form-control" placeholder="新增留言 . . ." name="newComment"
						id="exampleTextarea" rows="9" style="width:25vw; height: 200px; margin-right:20px"></textarea>
					<input type="hidden" name="workId" value="${works.works_id}">
					<input type="submit" class="btn btn-danger" name="submit" id="submitBtn"
						value="send" style="width: 50%; margin: 20px 15px;">
				</div>
				<div class="commentBox" style="display:inline-block">   
						<c:forEach var="workComment" items="${commentElements}">
							<div class="minbox" style="padding:5px; width:60vw; margin:5px 0px">
								<p style="color:white">${workComment.userNickName} :  ${workComment.comment}</p>  
							</div>
						</c:forEach>
				</div>
			</div>

		</form>
	</div>
</div>

 	<script src="https://code.jquery.com/jquery-3.3.1.min.js" ></script>
    <script>
    $("#submitBtn").click(function(e) {
    	 e.preventDefault();
    	 var userName = "undefined";
    	 var comment = $('#exampleTextarea').val();
    	 var str = "<div class='minbox' style='padding:5px; width:60vw; margin:5px 0px'>"+
			"<p style='color:white'>"+userName+ ":"+comment +"</p></div>" ;			
    	alert('submit')        
        $.ajax({
               type: "POST",
               url: 'updateComment',
               cache:false,
               /* dataType: 'json',      */ 
               data: $('#uppdateComment').serialize(), // serializes the form's elements.
               success: function(data)
               {
            	   alert('success')
            	   $('#commentBox').append(str);            	  
                   $('#exampleTextarea').val("");
                   
               },
               error: function(data){
            	   alert('error')
               }
             });
        	
    });
    
  //Create a lightbox
    (function() {
        $('.lightbox-gallery img').click(function(e) {
          var src = $(this).attr('src');          
          e.preventDefault();   
          var $lightbox = $("<div class='lightbox' style='text-align:center; margin:0px'></div>");          
          var $caption = $("<p class='caption'></p>");
          carousel = "<div style='margin:0px auto'>"+    					  
    					      "<div>"+
    					        "<img src='"+src+"'style='height:85vh'>"+
    					      "</div>"+   					 
    					  "</div>";
          var $carousel = $(carousel)
          // Add image and caption to lightbox
          $lightbox       
            .append($caption)
            .append($carousel);      
          // Add lighbox to document   
          $('body').append($lightbox);

          // Get image link and description
          var cap = $(this).attr("alt"); 
          // Add data to lighbox
          $caption.text(cap);
          // Show lightbox
          $lightbox.fadeIn('fast');
      
          $lightbox.click(function() {
          $lightbox.fadeOut('fast');
          });
        });
      }());
    $(document).ready(function(){
    	   img1 = document.getElementById('img1');
    	   img2 = document.getElementById('img2');
    	   img3 = document.getElementById('img3');
    	   setPWidth(img1, img2, img3);
    	   setImgHeight(img1, img2, img3);
    	   window.addEventListener('resize', function () {
    	   setPWidth(img1, img2, img3);
    	   setImgHeight(img1, img2, img3);
    	   });
   
    });  
   function setPWidth(img1, img2, img3){
	   	var width1 = img1.clientWidth;
	   	var width2 = img2.clientWidth;
	   	var width3 = img3.clientWidth;
	   	$('#intro1').width(width1);
	   	$('#intro2').width(width2);
	   	$('#intro3').width(width3);
   }
   function setImgHeight(){
		var width1 = img1.clientWidth;
	   	var width2 = img2.clientWidth;
	   	var width3 = img3.clientWidth;
		var height1 = img1.clientHeight;
	   	var height2 = img2.clientHeight;
	   	var height3 = img3.clientHeight;
	   	var totalWidth = width1+width2+width3
	   	var winWidth = window.screen.width ;
	   	if(totalWidth > winWidth){
	   		$('#img1').css('height','45vh');  
	   		$('#img2').css('height','45vh');
	   		$('#img3').css('height','45vh');
	   	}
   }
    </script>
<jsp:include page="/fragment/footer.jsp" />