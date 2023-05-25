package com.github.foxnic.springboot.mvc;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ResourceMatchService<R> {
    List<R> getMatched(HttpServletRequest request);
}
