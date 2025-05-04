package raf.rs.domaci.web3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import raf.rs.domaci.web3.model.ErrorMessage;
import raf.rs.domaci.web3.repositories.ErrorMessageRepository;
import raf.rs.domaci.web3.response.ErrorMessageResponse;

@Service
public class ErrorMessageService {

    @Autowired
    private ErrorMessageRepository errorMessageRepository;

    public Page<ErrorMessageResponse> getAllErrors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        return errorMessageRepository.findAll(pageable)
                .map(ErrorMessageResponse::fromEntity);
    }
}