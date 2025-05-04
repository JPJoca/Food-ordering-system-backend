package raf.rs.domaci.web3.controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import raf.rs.domaci.web3.response.ErrorMessageResponse;
import raf.rs.domaci.web3.service.ErrorMessageService;

@RestController
@RequestMapping("/api/errors")
public class ErrorMessageController {

    @Autowired
    private ErrorMessageService errorMessageService;

    @PreAuthorize("hasAuthority('can_track_order')")
    @GetMapping
    public Page<ErrorMessageResponse> getErrors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return errorMessageService.getAllErrors(page, size);
    }
}