package com.lj.dto;

import java.io.Serializable;

public abstract class AbstractDto implements Serializable {

    private static final long serialVersionUID = 1L;

     protected AbstractDto getObjectDto(Object ob) {
         return null;
     }

}
