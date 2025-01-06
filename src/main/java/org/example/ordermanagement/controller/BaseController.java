package org.example.ordermanagement.controller;

import org.example.ordermanagement.utils.ReceiveToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Map;

public abstract class BaseController {

    @Autowired
    protected ReceiveToken receiveToken;

    protected Map<String, String> userData;

    @ModelAttribute
    public void initData() {
        this.userData = receiveToken.tokenData();
    }
}


