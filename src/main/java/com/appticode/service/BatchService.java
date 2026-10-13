package com.appticode.service;

import com.appticode.entity.Batch;
import com.appticode.repository.BatchRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BatchService {

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    public Batch createBatch(Batch batch) {
        batch.setBatchId(idGenerator.generateBatchId());
        batch.setCurrentCount(0);
        batch.setCreatedAt(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    public Optional<Batch> updateBatch(String batchId, Batch batchDetails) {
        Optional<Batch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isPresent()) {
            Batch batch = batchOpt.get();
            if (batchDetails.getBatchName() != null) {
                batch.setBatchName(batchDetails.getBatchName());
            }
            if (batchDetails.getBatchCode() != null) {
                batch.setBatchCode(batchDetails.getBatchCode());
            }
            if (batchDetails.getDescription() != null) {
                batch.setDescription(batchDetails.getDescription());
            }
            if (batchDetails.getStartDate() != null) {
                batch.setStartDate(batchDetails.getStartDate());
            }
            if (batchDetails.getEndDate() != null) {
                batch.setEndDate(batchDetails.getEndDate());
            }
            if (batchDetails.getMaxCapacity() != null) {
                batch.setMaxCapacity(batchDetails.getMaxCapacity());
            }
            batch.setUpdatedAt(LocalDateTime.now());
            return Optional.of(batchRepository.save(batch));
        }
        return Optional.empty();
    }

    public Optional<Batch> getBatchById(String batchId) {
        return batchRepository.findById(batchId);
    }

    public Optional<Batch> getBatchByCode(String batchCode) {
        return batchRepository.findByBatchCode(batchCode);
    }

    public List<Batch> getAllBatches() {
        return batchRepository.findAll();
    }

    public Page<Batch> getAllBatches(Pageable pageable) {
        return batchRepository.findAll(pageable);
    }

    public List<Batch> getActiveBatches() {
        return batchRepository.findActiveBatches(LocalDate.now());
    }

    public List<Batch> getUpcomingBatches() {
        return batchRepository.findUpcomingBatches(LocalDate.now());
    }

    public List<Batch> getBatchesWithCapacity() {
        return batchRepository.findBatchesWithCapacity();
    }

    public boolean deleteBatch(String batchId) {
        Optional<Batch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isPresent()) {
            Batch batch = batchOpt.get();
            if (batch.getCurrentCount() == 0) {
                batchRepository.delete(batch);
                return true;
            }
        }
        return false;
    }

    public boolean existsByBatchCode(String batchCode) {
        return batchRepository.existsByBatchCode(batchCode);
    }

    public void incrementCurrentCount(String batchId) {
        batchRepository.incrementCurrentCount(batchId);
    }

    public void decrementCurrentCount(String batchId) {
        batchRepository.decrementCurrentCount(batchId);
    }

    public Page<Batch> getAllBatchesOrderByStartDateDesc(Pageable pageable) {
        return batchRepository.findAllOrderByStartDateDesc(pageable);
    }
}