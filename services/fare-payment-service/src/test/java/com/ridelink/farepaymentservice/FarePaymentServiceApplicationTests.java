package com.ridelink.farepaymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ridelink.farepaymentservice.repository.FareRepository;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
import com.ridelink.farepaymentservice.repository.ReceiptRepository;

@SpringBootTest
class FarePaymentServiceApplicationTests {

    @MockBean
    private FareRepository fareRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private ReceiptRepository receiptRepository;

	@Test
	void contextLoads() {
	}

}
