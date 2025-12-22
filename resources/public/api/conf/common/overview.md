Use this API to check whether a purchasing operator is approved to buy vaping stamps and to retrieve the operator’s current approval details for compliance purposes.

The API supports the following use case:
- verifying an operator’s approval status before issuing vaping stamps

The API returns the operator’s approval status and related business information. The stamp supplier uses this information to decide whether stamps can be issued and within what limits.

This API is read-only and provides the current approval state only. It does not provide historical data, notifications, or real-time change events.

The Vaping Stamps service also includes a secure bulk file upload process for weekly data submission, including stamp sales and scanning data. This bulk upload process forms part of the overall service but operates independently of the API.

### Request access to the API
This is a restricted-access API. The endpoint is available only to authorised and approved software applications.
Because this API is restricted, you must request access before your software application can be subscribed.
### Who can request access
To request access, you must:
- be the commercial stamp supplier appointed by HMRC
- have an HMRC Developer Hub account with a registered software application
  HMRC may ask you to provide evidence of your organisation’s role or relationship to confirm eligibility.
  If you do not have a Developer Hub account, [you can register for one on GOV.UK](https://developer.service.hmrc.gov.uk/developer/registration).
  Your account must use a work email address.
### How to request access
Once your Developer Hub account and software application are set up:
1. [Sign in to HMRC Developer Hub](https://developer.service.hmrc.gov.uk/developer/login)
2. Return to this API landing page.
3. Go to the Endpoint section and select Request access.
4. Complete the request form with:
    - your organisation name
    - the API name: Vaping Stamps API
    - the application ID for your Developer Hub software application 

HMRC may contact you to discuss your request and confirm eligibility. 

If your request is approved, you will receive a confirmation email and your software application will be subscribed to the API.

If you are not signed in, or access has not yet been granted, the Endpoint section will not display a subscription link. You may see 'Not applicable' or 'Sign in to request access' instead.
