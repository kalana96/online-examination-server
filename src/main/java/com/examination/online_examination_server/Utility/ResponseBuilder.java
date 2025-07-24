package com.examination.online_examination_server.Utility;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
    public static ResponseEntity<ResponseDTO> buildSuccessResponse(String message, Object content) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setCode(VarListt.RES_SUCCESS);
        responseDTO.setMessage(message);
        responseDTO.setContent(content);
        responseDTO.setSuccess(true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public static ResponseEntity<ResponseDTO> buildErrorResponse(String code, String message, HttpStatus status) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setCode(code);
        responseDTO.setMessage(message);
        responseDTO.setContent(null);
        responseDTO.setSuccess(false);
        return new ResponseEntity<>(responseDTO, status);
    }
}
