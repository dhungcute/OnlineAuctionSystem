package com.auction.service;

import com.auction.core.model.BidTransaction;
import com.auction.core.model.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// các sự kiện đặt giá (bid) mới từ phiên đấu giá (Subject) và tự động ghi lại lịch sử (log) ra hệ thống
public class BidLoggingObserver implements Observer {
    // Khởi tạo đối tượng Logger từ thư viện SLF4J để ghi nhật ký hoạt động của lớp này
    private static final Logger log = LoggerFactory.getLogger(BidLoggingObserver.class);

    @Override

    public void update(BidTransaction newBid) {
        log.info("[Observer] Lượt Bid mới: {} VND được đặt bởi user '{}' trên phiên '{}'",
                newBid.getAmount(),
                newBid.getBidder().getUsername(),
                newBid.getAuction().getId());
    }
}