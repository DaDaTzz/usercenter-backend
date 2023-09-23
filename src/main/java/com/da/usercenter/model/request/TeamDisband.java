package com.da.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamDisband implements Serializable {

    private static final long serialVersionUID = 6286327918183750049L;
    private Long teamId;
}
