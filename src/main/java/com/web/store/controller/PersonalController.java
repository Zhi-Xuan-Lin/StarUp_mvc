package com.web.store.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.web.store.model.FormBean;
import com.web.store.model.UserBean;
import com.web.store.model.WorkCommentBean;
import com.web.store.model.WorksBean;
import com.web.store.service.FormService;
import com.web.store.service.UserService;
import com.web.store.service.WorksService;
@Controller
public class PersonalController {
	@Autowired
	ServletContext context;
	@Autowired
	UserService userService;
	@Autowired
	WorksService worksService;
	@Autowired
	FormService formService;
	
	public PersonalController() {
		
	}
	@RequestMapping(value="/personalPage")
	public String getPersonalPage(@RequestParam(value = "id") Integer userId, Model model) {
		UserBean ub = userService.getUser(userId);
		Clob introduction = ub.getIntroduction();
		String intro = null;
		try {
			intro = introduction.getSubString(1, (int) ub.getIntroduction().length());
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		model.addAttribute("userBean", ub);
		model.addAttribute("introduction", intro);
		return "_10_personalPage/personalPage";
	}

	@RequestMapping(value="/worksList")
	public String getWorkList() {
		return "_06_workUp/worksList";
	}
	@RequestMapping(value="/upload")
	public String getUploadForm() {   
		return "_06_workUp/upload";
	}
	@RequestMapping(value="/userWorks", method=RequestMethod.POST)
	public @ResponseBody byte[] listUserWorks(@RequestParam(value="userId") Integer userId) throws UnsupportedEncodingException {
		System.out.println("111111");
		List<WorksBean> list = worksService.getWorksByUserId(userId);
		byte[] worksJson = new Gson().toJson(list).getBytes("UTF-8");
		return worksJson;	
	}
	/* 測試跳轉作品詳細頁面 */
	@RequestMapping(value="/testComment")
	public String testComment(Model model, @RequestParam("worksId") Integer worksId) {
		WorksBean wb = null;
		List<WorkCommentBean> list = new ArrayList<>();
		if (worksId != null) {
			wb = worksService.getWorksById(worksId);			
		}
		if (wb != null && wb.getComment() != null) {
			WorkCommentBean wcb = null;
			Clob totalComment = wb.getComment();
			try {
				String totalCommentStr = totalComment.getSubString(1, (int)totalComment.length() - 2);
				String[] oneCommentElementStr = null;
				if (totalCommentStr.contains("-#")) {
					String[] totalCommentStrArr = totalCommentStr.split("-#");
					for(String oneCommentStr : totalCommentStrArr) {
						oneCommentElementStr = oneCommentStr.split("/=");
						wcb = new WorkCommentBean(
								Integer.parseInt(oneCommentElementStr[0]), 
								oneCommentElementStr[1], 
								oneCommentElementStr[2], 
								oneCommentElementStr[3]);
						list.add(wcb);
					}
				} else {
					oneCommentElementStr = totalCommentStr.split("/=");
					wcb = new WorkCommentBean(
							Integer.parseInt(oneCommentElementStr[0]), 
							oneCommentElementStr[1], 
							oneCommentElementStr[2], 
							oneCommentElementStr[3]);
					list.add(wcb);
				}
				model.addAttribute("commentsElement", list);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		model.addAttribute("oneWork", wb);
		return "_06_workUp/works";
	}
	/* 測試存入留言 */
	@RequestMapping(value="/updateComment", params= {"newComment", "workId"}, method=RequestMethod.POST, 
			produces="text/html;charset=UTF-8")
	public String updateComment(HttpServletRequest request, Model model, 
			@RequestParam("workId")String workIdStr, 
			@RequestParam("newComment")String newComment) {
		WorksBean wb = null;
		if(workIdStr != null) {
			Integer works_id = Integer.parseInt(workIdStr);
			wb = worksService.getWorksById(works_id);
		}
		UserBean ub = (UserBean)request.getSession(false).getAttribute("LoginOK");
		Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		String commentDate = ts.toString();
		//String comment = request.getParameter("newComment");
		String totalCommentStrNew = "";
		if (wb.getComment() != null) {
			Clob totalComment = wb.getComment();
			try {
				String totalCommentStr = totalComment.getSubString(1, (int)totalComment.length());
				totalCommentStrNew = totalCommentStr + ub.getUser_id() +"/=" +
						ub.getNickname() +"/=" + commentDate + "/=" + newComment + "-#";
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			totalCommentStrNew = ub.getUser_id() + "/=" + ub.getNickname() + "/=" + 
								commentDate + "/=" + newComment + "-#";
		}
		try {
			Clob totalCommentNew = new SerialClob(totalCommentStrNew.toCharArray());
			wb.setComment(totalCommentNew);
			worksService.updateWorksComment(wb);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "redirect:/testComment";
	}
	
//	-------------------申請商品上架表單ajax-------------------
	@RequestMapping(value="/addForm", method=RequestMethod.POST)	
	public ResponseEntity<FormBean> processAddForm(HttpServletRequest request,@ModelAttribute FormBean fb) throws SerialException, SQLException {
		System.out.println(fb);		
			MultipartFile formImage = fb.getFormImage();
			String originalFilename = formImage.getOriginalFilename();
			String rootDirectory = request.getSession().getServletContext().getRealPath("/");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = df.format(new Date());
			fb.setSubmitDate(date);
			//建立Blob物件，交由Hibernate寫入資料庫
			if(formImage != null && !formImage.isEmpty()) {
				fb.setFormImgName(originalFilename);
				String ext = originalFilename.substring(originalFilename.lastIndexOf("."));	
				try {
					byte[] b = formImage.getBytes();
					Blob blob = new SerialBlob(b);
					fb.setFormImg(blob);
				}catch(Exception e) {
					e.printStackTrace();
					throw new RuntimeException("檔案上傳發生異常"+ e.getMessage());
				}
				try {
					File imageFolder = new File(rootDirectory+"images");
					if(!imageFolder.exists()) {
						imageFolder.mkdirs();
					}
					File file = new File(imageFolder, fb.getForm_id()+ext);
					formImage.transferTo(file);
				}catch(Exception e) {
					e.printStackTrace();
					throw new RuntimeException("檔案上傳發生異常"+ e.getMessage());
				}
			}		
			formService.processForm(fb);
			return new ResponseEntity<FormBean>(HttpStatus.OK);
	}
}
