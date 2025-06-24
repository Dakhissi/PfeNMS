package com.farukgenc.boilerplate.springboot.service.discovery;

import com.farukgenc.boilerplate.springboot.dto.DiscoveryProgressDto;
import java.util.function.Consumer;

/**
 * Interface for reporting discovery progress
 */
@FunctionalInterface
public interface ProgressCallback {
    /**
     * Called when progress is updated
     * @param progress The current progress information
     */
    void onProgressUpdate(DiscoveryProgressDto progress);
}
