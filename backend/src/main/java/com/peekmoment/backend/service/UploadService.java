package com.peekmoment.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.peekmoment.backend.common.BusinessException;
import com.peekmoment.backend.common.ResultCode;

/**
 * 本地文件上传：按日期分目录，返回相对访问路径。
 */
@Service
public class UploadService {

    private static final Set<String> ALLOWED =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "mp4", "mov");

    @Value("${peekmoment.upload.dir}")
    private String dir;

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件为空");
        }
        String original = file.getOriginalFilename();
        String ext = original == null || !original.contains(".")
                ? "" : original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED.contains(ext)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的文件类型");
        }
        try {
            String relative = LocalDate.now() + "/" + UUID.randomUUID() + "." + ext;
            Path target = Paths.get(dir).toAbsolutePath().resolve(relative);
            Files.createDirectories(target.getParent());
            file.transferTo(target);
            return "/uploads/" + relative;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "文件保存失败");
        }
    }
}
