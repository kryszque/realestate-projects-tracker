package com.mcdevka.realestate_projects_tracker.domain.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignCompanyRequest {
    // 👇 Zmieniamy z String companyName na listę ID
    private List<Long> companyIds;
}