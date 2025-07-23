package gift.option.controller;

import gift.option.dto.OptionAddRequest;
import gift.option.dto.OptionResponse;
import gift.option.dto.OptionUpdateRequest;
import gift.option.service.OptionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/options")
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @PostMapping
    public ResponseEntity<OptionResponse> addOption(
        @PathVariable Long productId,
        @Valid @RequestBody OptionAddRequest optionAddRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(optionService.addOption(productId, optionAddRequest));
    }

    @GetMapping
    public ResponseEntity<List<OptionResponse>> getOptions(@PathVariable Long productId) {
        return ResponseEntity.ok(optionService.getOptions(productId));
    }

    @PatchMapping("/{optionId}")
    public ResponseEntity<OptionResponse> updateOption(
        @PathVariable Long productId,
        @PathVariable Long optionId,
        @Valid @RequestBody OptionUpdateRequest optionUpdateRequest) {
        return ResponseEntity.ok(
            optionService.updateOption(productId, optionId, optionUpdateRequest));
    }

    @DeleteMapping("/{optionId}")
    public ResponseEntity<String> deleteOption(
        @PathVariable Long productId,
        @PathVariable Long optionId) {
        optionService.deleteOption(productId, optionId);
        return ResponseEntity.ok("옵션 삭제가 완료되었습니다.");
    }
}
