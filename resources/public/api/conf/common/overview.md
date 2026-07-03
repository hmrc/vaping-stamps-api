The Vaping Duty Stamps - Approval Status Check API enables authorised stamp suppliers to verify whether a purchasing operator is approved to purchase vaping stamps.

The business use case of developing this API is to support the verification of a purchasing operator’s approval status before an authorised stamp supplier issues the vaping duty stamps.

Using this secure and standardised API, the stamp suppliers can access the following information about the stamps purchasing operator:
 - their current approval status
 - associated business data

The above information helps authorised stamp suppliers to make an informed decision prior to the issuance of the vaping duty stamps to the operator.

The approval status returned by this API may also influence the purchasing eligibility of the operator. The approval status enables the stamp suppliers to determine whether the vaping duty stamps may be issued and, where applicable, whether any purchasing restrictions, conditions, or limits apply.

The Vaping Duty Stamps - Approval Status Check API supports operational decision-making during the stamp issuance process. It provides access to the most up-to-date approval status of the purchasing operator, available at the point in time the request for stamps issuance is raised.

This is a read-only API and does not permit the creation, modification, or removal of the purchasing operator’s approval records.

The Vaping Duty Stamps - Approval Status Check API does not provide:
  - historical approval status
  - approval status change notifications
  - audit trails or approval history
  - approval management or status update capabilities

**Features**

Primary features of this API include the:
  - retrieval of the current approval status of a purchasing operator
  - support for eligibility validation and issuance decisions pertaining to the vaping duty stamps
  - identification of applicable purchasing restrictions or limits
  - read-only access to current approval status of the operator
  - standardised response mechanisms and error handling techniques

**Benefits**

Primary benefits of this API include:
  - improved supplier self-service
  - reduced support requests
  - faster integration of external and internal systems
  - reliable and secure interactions between the various systems
  - improved data security and governance

**Intended Audience**

The Vaping Duty Stamps - Approval Status Check API is intended for the following that are involved in the validation and issuance of vaping duty stamps, such as:
  - authorised stamp suppliers
  - systems responsible for validating purchasing operator eligibility prior to stamp issuance
  - business applications requiring access to operator approval status
  - integration platforms that support operational compliance and purchasing validation processes
  - Users of this Vaping Duty Stamps - Approval Status Check API are expected to handle the API-returned approval status as part of their business processes and must make sure that the vaping duty stamps are issued only to eligible operators.

**API behaviour**

The Vaping Duty Stamps - Approval Status Check API enables suppliers to verify a purchasing operator's approval status before completing a transaction. The end-to-end journey is as follows:
  1. The vaping duty stamps supplier submits an approval request for a specific purchasing operator, who has already been assigned a unique stampsReferenceNumber by HMRC.
  1. Upon receipt, the Vaping Duty Stamps - Approval Status Check API authenticates the request and validates it.
  1. The API verifies the authenticity of the request and performs the following:
     1. validates the request
     1. attempts to identify the corresponding operator data from HMRC
  1. When a matching operator is identified, the Vaping Duty Stamps - Approval Status Check API retrieves the current approval status from HMRC.
  1. The Vaping Duty Stamps - Approval Status Check API returns a response with the following information:
     1. An appropriate response code indicating the outcome of the request
     1. the purchasing operator’s current approval status
     1. any relevant business information associated with that approval
The authorised stamp supplier uses the response to:
  - verify if the purchasing operator is authorised to purchase the vaping duty stamps
  - determine if any approval conditions, purchasing restrictions, or limits apply
  - prevent the issuance of the vaping duty stamp where approval requirements are not satisfied
  - support other operational compliance and eligibility verification processes
The response returned by the Vaping Duty Stamps - Approval Status Check API represents the purchasing operator’s approval status at the time the request is processed. As approval status may change over time, the response should not be relied upon as an evidence of ongoing authorisation beyond the point of processing.

The approval status can be verified only once for a specific stampsReferenceNumber.

*Note*
  * The authorised stamp supplier should perform the operator status checks in accordance with their business obligations.
  * the approval status can be verified only once per purchasing operator.






