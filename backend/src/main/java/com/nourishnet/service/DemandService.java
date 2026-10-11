package com.nourishnet.service;

import com.nourishnet.dto.DemandCreateRequest;
import com.nourishnet.exception.ResourceNotFoundException;
import com.nourishnet.exception.UnauthorizedException;
import com.nourishnet.model.DemandRequest;
import com.nourishnet.model.DemandStatus;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.repository.DemandRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DemandService {
    private final DemandRepository demandRepository;

    public DemandService(DemandRepository demandRepository) {
        this.demandRepository = demandRepository;
    }

    public DemandRequest createDemand(DemandCreateRequest request, User recipient) {
        if (recipient.getRole() != UserRole.RECIPIENT && recipient.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only registered recipients can publish food requests on the demand board");
        }

        String recipientName = recipient.getOrganization() != null && !recipient.getOrganization().isEmpty()
                ? recipient.getOrganization()
                : recipient.getName();

        DemandRequest demand = new DemandRequest(
                UUID.randomUUID().toString(),
                recipient.getId(),
                recipientName,
                request.getCategory(),
                request.getRequiredQuantity(),
                request.getUnit().trim(),
                request.getLocation().trim(),
                request.getUrgency(),
                request.getNotes()
        );

        return demandRepository.save(demand);
    }

    public List<DemandRequest> getAllDemands(DemandStatus status) {
        if (status != null) {
            return demandRepository.findByStatus(status);
        }
        return demandRepository.findAll();
    }

    public List<DemandRequest> getDemandsByRecipient(String recipientId) {
        return demandRepository.findByRecipientId(recipientId);
    }

    public DemandRequest fulfillDemand(String id, User user) {
        DemandRequest demand = demandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demand request not found: " + id));
        demand.setStatus(DemandStatus.FULFILLED);
        return demandRepository.save(demand);
    }
}
