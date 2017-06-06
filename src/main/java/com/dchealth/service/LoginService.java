package com.dchealth.service;

import org.springframework.stereotype.Controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * Created by Administrator on 2017/6/6.
 */
@Path("login")
@Produces("application/json")
@Controller
public class LoginService {
    @Context
    private HttpServletRequest httpServletRequest ;
    @Context
    private HttpServletResponse httpServletResponse;

    @GET
    public  void login() throws IOException, ServletException {
        httpServletResponse.sendRedirect("../index.html");
    }



}
