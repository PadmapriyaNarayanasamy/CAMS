package com.caps.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.caps.dao.AccountRoleDao;
import com.caps.dao.CourseDao;
import com.caps.dao.EnrollmentDao;
import com.caps.entity.Account;
import com.caps.entity.Accountrole;
import com.caps.entity.Course;
import com.caps.entity.Enrollment;
import com.caps.entity.EnrollmentPK;
import com.caps.service.AdminService;
import com.caps.service.StudentService;
import com.caps.util.UserUtil;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

	@Autowired
	AdminService adminService;

	@Autowired
	StudentService studentService;
	@Autowired
	EnrollmentDao enrollmentDao;

	@Autowired
	AccountRoleDao accountRoleDao;

	@Autowired
	CourseDao courseDao;

	@ModelAttribute
	public void setFormEmptyObject(Model model, HttpSession httpsession) {
		model.addAttribute("login", UserUtil.currentUser(httpsession));
	}

	@RequestMapping("/welcome")
	@ResponseBody
	public ModelAndView insertCustomers(HttpSession httpSession) {
		ModelAndView mav = new ModelAndView("/admin/welcome");
		mav.addObject("Text", UserUtil.currentUser(httpSession));
		return mav;
	}

	@RequestMapping("/findlecturer")
	@ResponseBody
	public ModelAndView insertCustomers(Model model) {
		ModelAndView mav = new ModelAndView("/admin/Listlecturer");
		mav.addObject("lecturer", new Account());

		return mav;
	}

	@RequestMapping("/findCourses")
	@ResponseBody
	public ModelAndView displayCourses(Model model) {
		ModelAndView mav = new ModelAndView("/admin/list-course");
		mav.addObject("course", new Course());
		mav.addObject("courselist", adminService.findByType("ROLE_LECTURER"));
		return mav;
	}

	@RequestMapping("/findstudents")
	@ResponseBody
	public ModelAndView displayStudents(Model model) {
		ModelAndView mav = new ModelAndView("/admin/liststudent");
		model.addAttribute("student", new Account());
		return mav;
	}

	@RequestMapping("/addcourse")
	@ResponseBody
	public ModelAndView addCourse(Model model) {
		ModelAndView mav = new ModelAndView("admin/add-course");
		model.addAttribute("course", new Course());
		mav.addObject("courselist", adminService.findByType("ROLE_LECTURER"));

		return mav;
	}

	@RequestMapping("/addstudent")
	@ResponseBody
	public ModelAndView addStudent(Model model) {
		ModelAndView mav = new ModelAndView("admin/add-student");
		model.addAttribute("student", new Account());
		// mav.addObject("courselist", adminService.findByType("lecturer"));
		return mav;
	}

	@RequestMapping(value = "/deleteCourse/{courseID}", method = RequestMethod.GET)
	public ModelAndView deleteCourse(@PathVariable String courseID, final RedirectAttributes redirectAttributes,
			HttpSession httpsession) {
		/*Course course = adminService.findByCourseId(Integer.parseInt(courseID));
		EnrollmentPK e = new EnrollmentPK();
		String userid = UserUtil.currentUser(httpsession);
		e.setCourseid(Integer.parseInt(courseID));
		e.setUserid(Integer.parseInt(userid));
		Enrollment en = adminService.findByEnrollmentId(e);
		System.out.println(en.getCourse().getCourseid());*/
		List<Enrollment> enrollment = enrollmentDao.findByIdCourseid(Integer.parseInt(courseID));
		if(!enrollment.isEmpty())
		{
			for (Enrollment e : enrollment) {
				enrollmentDao.delete(e);
		}
		}
		adminService.delete(Integer.parseInt(courseID));
		ModelAndView mav = new ModelAndView("redirect:/admin/findCourses");

		//String message = "The Course " + course.getCourseName() + " was successfully deleted.";
		//redirectAttributes.addFlashAttribute("message", message);
		return mav;
	}

	@RequestMapping(value = "/deleteStudent/{studentID}", method = RequestMethod.GET)
	public ModelAndView deleteStudent(@PathVariable String studentID, final RedirectAttributes redirectAttributes) {
		EnrollmentPK e = new EnrollmentPK();
		e.setUserid(Integer.parseInt(studentID));
		List<Enrollment> enrollment = adminService.findEnrollmentByStudentid(Integer.parseInt(studentID));
		for (Enrollment es : enrollment) {
			// result.add(userDao.findByUserid(e.getId().getUserid()));
			/*e.setUserid(Integer.parseInt(studentID));
			e.setCourseid(es.getCourse().getCourseid());
			System.out.println(es.getCourse().getCourseid());
			studentService.Delete(e);*/
			enrollmentDao.delete(es);
		}
		accountRoleDao.delete(Integer.parseInt(studentID));
		adminService.deleteStudent(Integer.parseInt(studentID));
		ModelAndView mav = new ModelAndView("redirect:/admin/findstudents");
		return mav;
	}

	@PostMapping("/NewCourse")
	public ModelAndView insertCourse(@ModelAttribute Course course) {
		// DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		// course.setStartDate(df.parse(course.getStartDate()));
		adminService.insertOrUpdate(course);
		ModelAndView mav = new ModelAndView("/admin/add-course");
		mav.addObject("course", new Course());
		return mav;
	}

	@PostMapping("/NewStudent")
	public ModelAndView insertStudent(@ModelAttribute Account account) {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		account.setPassword(bCryptPasswordEncoder.encode(account.getPassword()));
		account.setType("ROLE_STUDENT");
		account.setEnabled("true");
		Accountrole a = new Accountrole();
		a.setUserid(account.getUserid());
		a.setAuthority("ROLE_STUDENT");
		adminService.insertOrUpdate(account);
		adminService.insertOrUpdateRole(a);

		ModelAndView mav = new ModelAndView("/admin/add-student");
		mav.addObject("student", new Account());
		return mav;
	}

	@PostMapping("/UpdateCourse")
	public ModelAndView updateCourse(@ModelAttribute Course course) {
		// DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		// course.setStartDate(df.parse(course.getStartDate()));
		adminService.insertOrUpdate(course);
		ModelAndView mav = new ModelAndView("/admin/list-course");
		// mav.addObject("course", course);
		mav.addObject("course", new Course());
		mav.addObject("courselist", adminService.findByType("ROLE_LECTURER"));
		return mav;
	}

	@PostMapping("/UpdateStudent")
	public ModelAndView updateStudent(@ModelAttribute Account account) {
		// DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		// course.setStartDate(df.parse(course.getStartDate()));
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		account.setPassword(bCryptPasswordEncoder.encode(account.getPassword()));
		account.setType("ROLE_STUDENT");
		account.setEnabled("true");
		Accountrole a = new Accountrole();
		a.setUserid(account.getUserid());
		a.setAuthority("ROLE_STUDENT");
		adminService.insertOrUpdate(account);
		adminService.insertOrUpdateRole(a);
		ModelAndView mav = new ModelAndView("/admin/liststudent");
		// mav.addObject("course", course);
		mav.addObject("student", new Account());
		return mav;
	}

	@RequestMapping("/api/listlecturer")
	@ResponseBody
	public List<Account> listEnrollment() {
		return adminService.findByType("ROLE_LECTURER");
	}

	@RequestMapping("/api/listcourses")
	@ResponseBody
	public List<Course> listCourses() {
		return adminService.findAllCourses();
	}

	@RequestMapping("/api/liststudents")
	@ResponseBody
	public List<Account> listStudents() {
		return adminService.findByType("ROLE_STUDENT");
	}

	@RequestMapping(value = "/addLecturer")
	public ModelAndView createLecturers() {

		ModelAndView mav = new ModelAndView("admin/Newlecturerform");
		mav.addObject("lecturer", new Account());
		return mav;
	}

	@PostMapping("/LecturerForm")
	public ModelAndView insertCustomers(@ModelAttribute Account lecturer) {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		lecturer.setPassword(bCryptPasswordEncoder.encode(lecturer.getPassword()));
		lecturer.setType("ROLE_LECTURER");
		lecturer.setEnabled("true");
		Accountrole a = new Accountrole();
		a.setUserid(lecturer.getUserid());
		a.setAuthority("ROLE_LECTURER");
		adminService.insertOrUpdate(lecturer);
		adminService.insertOrUpdateRole(a);
		ModelAndView mav = new ModelAndView("admin/Listlecturer");
		mav.addObject("lecturer", new Account());
		return mav;
	}

	@RequestMapping(value = "/delete/{userid}", method = RequestMethod.GET)
	public ModelAndView deleteLecturer(@PathVariable String userid) {
		;
		List<Course> course = adminService.findCourseByLecturer(Integer.parseInt(userid));
		for (Course es : course) {
			courseDao.delete(es);
			
			}
		
		accountRoleDao.delete(Integer.parseInt(userid));
		adminService.deleteStudent(Integer.parseInt(userid));
		ModelAndView mav = new ModelAndView("redirect:/admin/findlecturer");
		mav.addObject("lecturer", new Account());
		return mav;
	}

	@PostMapping("/update")
	public ModelAndView updateLecturer(@ModelAttribute Account lecturer) {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		lecturer.setPassword(bCryptPasswordEncoder.encode(lecturer.getPassword()));
		lecturer.setType("ROLE_LECTURER");
		lecturer.setEnabled("true");
		Accountrole a = new Accountrole();
		a.setUserid(lecturer.getUserid());
		a.setAuthority("ROLE_LECTURER");
		adminService.insertOrUpdate(lecturer);
		adminService.insertOrUpdateRole(a);
		ModelAndView mav = new ModelAndView("admin/Listlecturer");
		mav.addObject("lecturer", new Account());
		return mav;
	}

	@RequestMapping(value = "/editErollment/{userid}/{enrollmentDate}/{grades}/{courseid}", method = RequestMethod.GET)
	public ModelAndView enrollmentSTUED(@PathVariable String userid, @PathVariable String enrollmentDate,
			@PathVariable String grades, @PathVariable String courseid) {
		ModelAndView mav = new ModelAndView("redirect:/admin/enrollment-student");
		mav.addObject("courseid", courseid);
		mav.addObject("userid", userid);
		mav.addObject("enrolldate", enrollmentDate);
		mav.addObject("grades", grades);
		adminService.updateEnrollment(Integer.parseInt(courseid), Integer.parseInt(userid), Integer.parseInt(grades),
				enrollmentDate);
		return mav;
	}

	@RequestMapping(value = "/deleteErollment/{userid}/{courseid}", method = RequestMethod.GET)
	public ModelAndView enrollmentSTUDL(@PathVariable String userid, @PathVariable String courseid) {
		ModelAndView mav = new ModelAndView("redirect:/admin/enrollment-student");
		mav.addObject("courseid", courseid);
		mav.addObject("userid", userid);
		adminService.removeEnrollment(Integer.parseInt(userid));
		return mav;
	}

	@RequestMapping(value = "/addEnrollment/{userid}/{courseid}", method = RequestMethod.GET)
	public ModelAndView addenrollment(@PathVariable String userid, @PathVariable String courseid) {
		ModelAndView mav = new ModelAndView("redirect:/admin/enrollment-student");
		mav.addObject("userid", userid);
		mav.addObject("courseid", courseid);
		Date date = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		mav.addObject("date", ft.format(date));
		adminService.addEnrollment(Integer.parseInt(courseid), Integer.parseInt(userid), 0, ft.format(date));
		return mav;
	}

	@RequestMapping("/enrollment")
	public ModelAndView enrollment(HttpSession httpSession) {
		ModelAndView mav = new ModelAndView("/admin/enrollment-course");
		mav.addObject("Text", UserUtil.currentUser(httpSession));
		return mav;
	}

	@RequestMapping("/enrollment-student")
	public ModelAndView enrollmentSTU(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("/admin/enrollment-student");
		mav.addObject("courseid", request.getParameter("courseid"));
		return mav;
	}

	@RequestMapping("/api/enrollment-course")
	@ResponseBody
	public List<Course> listCourse() {
		return adminService.findAllCourses();
	}

	@RequestMapping("/api/enrollment-student")
	@ResponseBody
	public List<Enrollment> listEnrollmentStu(HttpServletRequest request) {
		int courseid = Integer.parseInt(request.getParameter("courseid"));
		return adminService.findEnrollment(courseid);
	}

	@RequestMapping("/api/enrollment-stuList")
	@ResponseBody
	public List<Account> listEnrollmentStud(HttpServletRequest request) {
		int courseid = Integer.parseInt(request.getParameter("courseid"));
		List<Account> ac = adminService.findStuNotenroll(courseid);
		return ac;
	}

}
