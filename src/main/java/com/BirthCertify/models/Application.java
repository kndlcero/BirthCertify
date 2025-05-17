package com.birthcertify.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private UUID applicationId;
    private String applicantId;
    private int numberOfCopies;
    private String birthReferenceNumber; // Optional

    private String ownerLastName;
    private String ownerFirstName;
    private String ownerMiddleName;
    private LocalDate ownerDateOfBirth;
    private String ownerPlaceOfBirth;
    private String cityOfBirth;

    private String fatherLastName;
    private String fatherFirstName;
    private String fatherMiddleName;

    private String motherMaidenName;
    private String motherFirstName;
    private String motherMiddleName;

    private String requesterLastName;
    private String requesterFirstName;
    private String requesterMiddleInitial;
    private String requesterContactNo;
    private String signatureUrl;

    private String status; // DRAFT, PENDING, etc.

    // Getters & Setters
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { 
        this.applicationId = applicationId;
        logger.debug("Set applicationId: {}", applicationId);
    }

    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { 
        this.applicantId = applicantId; 
        logger.debug("Set applicantId: {}", applicantId);
    }

    public int getNumberOfCopies() { return numberOfCopies; }
    public void setNumberOfCopies(int numberOfCopies) { 
        this.numberOfCopies = numberOfCopies; 
        logger.debug("Set numberOfCopies: {}", numberOfCopies);
    }

    public String getBirthReferenceNumber() { return birthReferenceNumber; }
    public void setBirthReferenceNumber(String birthReferenceNumber) { 
        this.birthReferenceNumber = birthReferenceNumber; 
        logger.debug("Set birthReferenceNumber: {}", birthReferenceNumber);
    }

    public String getOwnerLastName() { return ownerLastName; }
    public void setOwnerLastName(String ownerLastName) { 
        this.ownerLastName = ownerLastName; 
        logger.debug("Set ownerLastName: {}", ownerLastName);
    }

    public String getOwnerFirstName() { return ownerFirstName; }
    public void setOwnerFirstName(String ownerFirstName) { 
        this.ownerFirstName = ownerFirstName; 
        logger.debug("Set ownerFirstName: {}", ownerFirstName);
    }

    public String getOwnerMiddleName() { return ownerMiddleName; }
    public void setOwnerMiddleName(String ownerMiddleName) { 
        this.ownerMiddleName = ownerMiddleName; 
        logger.debug("Set ownerMiddleName: {}", ownerMiddleName);
    }

    public LocalDate getOwnerDateOfBirth() { return ownerDateOfBirth; }
    public void setOwnerDateOfBirth(LocalDate ownerDateOfBirth) { 
        this.ownerDateOfBirth = ownerDateOfBirth; 
        logger.debug("Set ownerDateOfBirth: {}", ownerDateOfBirth);
    }

    public String getOwnerPlaceOfBirth() { return ownerPlaceOfBirth; }
    public void setOwnerPlaceOfBirth(String ownerPlaceOfBirth) { 
        this.ownerPlaceOfBirth = ownerPlaceOfBirth; 
        logger.debug("Set ownerPlaceOfBirth: {}", ownerPlaceOfBirth);
    }

    public String getCityOfBirth() { return cityOfBirth; }
    public void setCityOfBirth(String cityOfBirth) { 
        this.cityOfBirth = cityOfBirth; 
        logger.debug("Set cityOfBirth: {}", cityOfBirth);
    }

    public String getFatherLastName() { return fatherLastName; }
    public void setFatherLastName(String fatherLastName) { 
        this.fatherLastName = fatherLastName; 
        logger.debug("Set fatherLastName: {}", fatherLastName);
    }

    public String getFatherFirstName() { return fatherFirstName; }
    public void setFatherFirstName(String fatherFirstName) { 
        this.fatherFirstName = fatherFirstName; 
        logger.debug("Set fatherFirstName: {}", fatherFirstName);
    }

    public String getFatherMiddleName() { return fatherMiddleName; }
    public void setFatherMiddleName(String fatherMiddleName) { 
        this.fatherMiddleName = fatherMiddleName; 
        logger.debug("Set fatherMiddleName: {}", fatherMiddleName);
    }

    public String getMotherMaidenName() { return motherMaidenName; }
    public void setMotherMaidenName(String motherMaidenName) { 
        this.motherMaidenName = motherMaidenName; 
        logger.debug("Set motherMaidenName: {}", motherMaidenName);
    }

    public String getMotherFirstName() { return motherFirstName; }
    public void setMotherFirstName(String motherFirstName) { 
        this.motherFirstName = motherFirstName; 
        logger.debug("Set motherFirstName: {}", motherFirstName);
    }

    public String getMotherMiddleName() { return motherMiddleName; }
    public void setMotherMiddleName(String motherMiddleName) { 
        this.motherMiddleName = motherMiddleName; 
        logger.debug("Set motherMiddleName: {}", motherMiddleName);
    }

    public String getRequesterLastName() { return requesterLastName; }
    public void setRequesterLastName(String requesterLastName) { 
        this.requesterLastName = requesterLastName; 
        logger.debug("Set requesterLastName: {}", requesterLastName);
    }

    public String getRequesterFirstName() { return requesterFirstName; }
    public void setRequesterFirstName(String requesterFirstName) { 
        this.requesterFirstName = requesterFirstName; 
        logger.debug("Set requesterFirstName: {}", requesterFirstName);
    }

    public String getRequesterMiddleInitial() { return requesterMiddleInitial; }
    public void setRequesterMiddleInitial(String requesterMiddleInitial) { 
        this.requesterMiddleInitial = requesterMiddleInitial; 
        logger.debug("Set requesterMiddleInitial: {}", requesterMiddleInitial);
    }

    public String getRequesterContactNo() { return requesterContactNo; }
    public void setRequesterContactNo(String requesterContactNo) { 
        this.requesterContactNo = requesterContactNo; 
        logger.debug("Set requesterContactNo: {}", requesterContactNo);
    }

    public String getSignatureUrl() { return signatureUrl; }
    public void setSignatureUrl(String signatureUrl) { 
        this.signatureUrl = signatureUrl; 
        logger.debug("Set signatureUrl: {}", signatureUrl);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        logger.debug("Set status: {}", status);
    }

    // Load from ResultSet
    public void loadFromResultSet(ResultSet rs) throws SQLException {
        this.applicationId = UUID.fromString(rs.getString("application_id"));
        this.applicantId = rs.getString("applicant_id");
        this.numberOfCopies = rs.getInt("number_of_copies");
        this.birthReferenceNumber = rs.getString("birth_reference_number");

        this.ownerLastName = rs.getString("owner_last_name");
        this.ownerFirstName = rs.getString("owner_first_name");
        this.ownerMiddleName = rs.getString("owner_middle_name");
        this.ownerDateOfBirth = rs.getDate("date_of_birth").toLocalDate();
        this.ownerPlaceOfBirth = rs.getString("place_of_birth");
        this.cityOfBirth = rs.getString("city_of_birth");

        this.fatherLastName = rs.getString("father_last_name");
        this.fatherFirstName = rs.getString("father_first_name");
        this.fatherMiddleName = rs.getString("father_middle_name");

        this.motherMaidenName = rs.getString("mother_maiden_name");
        this.motherFirstName = rs.getString("mother_first_name");
        this.motherMiddleName = rs.getString("mother_middle_name");

        this.requesterLastName = rs.getString("requester_last_name");
        this.requesterFirstName = rs.getString("requester_first_name");
        this.requesterMiddleInitial = rs.getString("requester_middle_initial");
        this.requesterContactNo = rs.getString("requester_contact_no");

        this.signatureUrl = rs.getString("signature_url");
        this.status = rs.getString("status");

        logger.info("Loaded BirthApplication from ResultSet with ID: {}", applicationId);
    }
}
