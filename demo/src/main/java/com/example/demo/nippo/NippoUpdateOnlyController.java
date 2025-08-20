package com.example.demo.nippo;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nippo")
public class NippoUpdateOnlyController {

    private final NippoUpdateOnlyService service;
    public NippoUpdateOnlyController(NippoUpdateOnlyService service) { this.service = service; }

    /** DBの変更は「名前」と「メモ」のみ（テンプレ '未設定' を埋める） */
    @PostMapping("/fill")
    public ResponseEntity<Void> fill(@Valid @RequestBody FillSlotRequest req) {
        service.fillSlot(req);
        return ResponseEntity.ok().build();
    }

    /** DBの変更は「メモ」のみ（既存行のメモ差し替え） */
    @PostMapping("/memo")
    public ResponseEntity<Void> memo(@Valid @RequestBody UpdateMemoRequest req) {
        service.updateMemo(req);
        return ResponseEntity.ok().build();
    }
}
