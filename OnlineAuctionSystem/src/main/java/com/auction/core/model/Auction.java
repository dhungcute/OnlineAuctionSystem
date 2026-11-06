package com.auction.core.model;

import com.auction.core.util.BidLadderUtil;
import com.auction.core.util.TimeSyncManager;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import com.auction.core.exception.InvalidBidException;
import com.auction.core.exception.InvalidStatusException;

// QUẢN LÝ CÁC HOẠT ĐỘNG ĐẤU GIÁ
public class Auction extends Entity implements Subject {
    private final Seller seller;
    private final Item item;
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double highestBid;
    private List<BidTransaction> bidHistory = new CopyOnWriteArrayList<>();
    private List<AutoBid> autoBids = new CopyOnWriteArrayList<>();
    private List<Observer> observers = new CopyOnWriteArrayList<>();

    // Khởi tạo phiên đấu giá nhanh với thời gian bắt đầu là hiện tại
    public Auction(Seller seller, Item item, LocalDateTime endTime) {
        this(seller, item, com.auction.core.util.TimeSyncManager.getNow(), endTime);
    }
    // Khởi tạo phiên đấu giá mới với thời gian bắt đầu và kết thúc cụ thể
    public Auction(Seller seller, Item item, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.seller = seller;
        this.item = item;
        this.highestBid = (item != null) ? item.getStartingPrice() : 0.0;
        this.startTime = startTime;
        this.endTime = endTime;
        LocalDateTime now = com.auction.core.util.TimeSyncManager.getNow();
        this.status = startTime != null && !startTime.isAfter(now)
                ? AuctionStatus.RUNNING
                : AuctionStatus.UPCOMING;
    }
    // Khởi tạo phiên đấu giá từ dữ liệu đã có sẵn (dùng khi load từ Database)
    public Auction(String id, LocalDateTime createdAt, Seller seller, Item item,
                   AuctionStatus status, double highestBid,
                   LocalDateTime startTime, LocalDateTime endTime) {
        super(id, createdAt);
        this.seller = seller;
        this.item = item;
        this.status = status;
        this.highestBid = highestBid;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    // Thêm một người dùng/hệ thống vào danh sách theo dõi phiên đấu giá
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    // Xóa một người dùng/hệ thống khỏi danh sách theo dõi
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    // Gửi thông báo đến tất cả những người đang theo dõi khi có lượt trả giá mới
    @Override
    public void notifyObserver(BidTransaction newBid) {
        for (Observer o : observers)
            o.update(newBid);
    }
    // Quản trị viên duyệt phiên đấu giá, chuyển từ "Đang chờ" sang "Sắp diễn ra"
    public void approveAuction() throws InvalidStatusException {
        if (this.status != AuctionStatus.PENDING && this.status != AuctionStatus.OPEN)
            throw new InvalidStatusException("Chỉ có thể duyệt phiên đang chờ. Trạng thái hiện tại: " + status);
        status = AuctionStatus.UPCOMING;
    }
    // Kích hoạt bắt đầu phiên đấu giá
    public void startAuction() throws InvalidStatusException {
        goLive();
    }
    // Đổi trạng thái sang "Đang diễn ra" và chốt thời gian bắt đầu
    public void goLive() throws InvalidStatusException {
        if (this.status != AuctionStatus.UPCOMING && this.status != AuctionStatus.OPEN)
            throw new InvalidStatusException("Chỉ có thể bắt đầu phiên sắp diễn ra. Trạng thái hiện tại: " + status);
        if (this.startTime == null)
            this.startTime = com.auction.core.util.TimeSyncManager.getNow();
        status = AuctionStatus.RUNNING;
    }
    // Xử lý lượt đặt giá mới. Có "synchronized" để chống lỗi mất dữ liệu khi nhiều người đặt giá cùng một lúc
    public synchronized void placeBid(BidTransaction newBid) throws InvalidBidException, InvalidStatusException {
        if (this.status != AuctionStatus.RUNNING)
            throw new InvalidStatusException("Phiên đấu giá chưa bắt đầu hoặc đã kết thúc.");
        if (this.endTime != null && com.auction.core.util.TimeSyncManager.getNow().isAfter(this.endTime))
            throw new InvalidStatusException("Phiên đấu giá đã kết thúc.");
        double newBidAmount = newBid.getAmount();
        double step = com.auction.core.util.BidLadderUtil.getIncrementForPrice(this.highestBid);
        if (newBidAmount < this.highestBid + step) {
            throw new InvalidBidException(String.format("Giá đặt tối thiểu phải là %,.0f ₫ (bước giá %,.0f ₫).", this.highestBid + step, step));
        }
        this.highestBid = newBidAmount;
        this.bidHistory.add(newBid);
        this.notifyObserver(newBid);
    }
    // Kết thúc phiên đấu giá đang chạy
    public void finishAuction() throws InvalidStatusException {
        if (status != AuctionStatus.RUNNING)
            throw new InvalidStatusException("Chỉ có thể kết thúc phiên đang chạy. Trạng thái hiện tại: " + status);
        status = AuctionStatus.CLOSED;
    }
    // Hủy phiên đấu giá (nếu chưa đóng hoặc chưa bị hủy trước đó)
    public void cancelAuction() throws InvalidStatusException {
        if (status == AuctionStatus.CLOSED)
            throw new InvalidStatusException("Không thể huỷ phiên đã đóng.");
        if (status == AuctionStatus.CANCELED)
            throw new InvalidStatusException("Phiên đấu giá đã bị huỷ rồi.");
        status = AuctionStatus.CANCELED;
    }
    // Lấy thông tin người bán
    public Seller getSeller() {
        return seller;
    }
    // Lấy thông tin sản phẩm đấu giá
    public Item getItem() {
        return item;
    }
    // Lấy trạng thái hiện tại của phiên
    public AuctionStatus getStatus() {
        return status;
    }
    // Lấy thời gian bắt đầu
    public LocalDateTime getStartTime() {
        return startTime;
    }
    // Lấy thời gian kết thúc
    public LocalDateTime getEndTime() {
        return endTime;
    }
    // Cập nhật lại thời gian kết thúc
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    // Lấy mức giá cao nhất hiện hành
    public double getHighestBid() {
        return highestBid;
    }
    // Cập nhật mức giá cao nhất (thường dùng khi setup dữ liệu)
    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }
    // Cập nhật trạng thái phiên
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
    // Cập nhật thời gian bắt đầu
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    // Lấy danh sách lịch sử trả giá (trả về bản sao để tránh bị chỉnh sửa lén từ bên ngoài)
    public List<BidTransaction> getBidHistory() {
        return new ArrayList<>(bidHistory);
    }
    // Tiêm/Thêm một lượt trả giá trực tiếp vào danh sách (dùng cho tải dữ liệu hoặc test)
    public void injectBid(BidTransaction bid) {
        bidHistory.add(bid);
    }
    // Tiêm/Thêm một cài đặt trả giá tự động
    public void injectAutoBid(AutoBid autoBid) {
        autoBids.add(autoBid);
    }
    // Lấy danh sách các cài đặt trả giá tự động
    public List<AutoBid> getAutoBids() {
        return new java.util.ArrayList<>(autoBids);
    }
    // Lấy ra lượt trả giá chiến thắng (lượt cuối cùng trong lịch sử)
    public BidTransaction getWinner() {
        if (bidHistory.isEmpty())
            return null;
        return bidHistory.get(bidHistory.size() - 1);
    }
    // Trả về tên trạng thái đã được dịch sang tiếng Việt để hiển thị lên màn hình
    public String getStatusDisplay() {
        return switch (status) {
            case UPCOMING, OPEN -> "Sắp diễn ra";
            case RUNNING -> "Đang diễn ra";
            case CLOSED -> "Đã đóng";
            case CANCELED -> "Đã huỷ";
            default -> status.name();
        };
    }
}
