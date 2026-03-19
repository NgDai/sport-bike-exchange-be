package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.EventBicycleCreationRequest;
import com.bicycle.marketplace.dto.response.CreateEventBicycleResponse;
import com.bicycle.marketplace.dto.response.CreatePostingResponse;
import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.mapper.PostingMapper;
import com.bicycle.marketplace.repository.*;
import com.bicycle.marketplace.dto.request.EventBicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.EventBicycleResponse;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.EventBicycleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class EventBicycleService {
    @Value("${vnpay.returnUrl}")
    private String vnpayReturnUrl;
    @Autowired
    private IEventBicycleRepository eventBicycleRepository;
    @Autowired
    private EventBicycleMapper eventBicycleMapper;
    @Autowired
    private PostingMapper postingMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IEventRepository eventRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;
    @Autowired
    private IBrandRepository brandRepository;
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private IBicycleRepository bicycleRepository;
    @Autowired
    private IWalletRepository walletRepository;
    @Autowired
    private IDepositRepository depositRepository;
    @Autowired
    private WalletTransactionService walletTransactionService;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private PostingService postingService;

    // public boolean checkStatusEvent(int eventId) {
    // boolean result = false;
    // Events event = eventRepository.findById(eventId).orElseThrow(() -> new
    // AppException(ErrorCode.EVENT_NOT_FOUND));
    // if (event.getStatus().equalsIgnoreCase("upcoming")) {
    // result = true;
    // }
    // return result;
    // }

    public EventBicycleResponse registerBicycleToEvent(int eventId, int listingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Events events = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        if (eventBicycleRepository.existsByListing_ListingId(listingId)) {
            throw new RuntimeException("Xe này đã được đăng ký vào sự kiện khác");
        }
        if (bikeListing.getSeller() == null || bikeListing.getSeller().getUserId() != user.getUserId()) {
            throw new RuntimeException("Bạn không có quyền đăng ký xe này");
        }

        Bicycle bicycle = bikeListing.getBicycle();

        if (events.getBikeType() != null && !events.getBikeType().equalsIgnoreCase("ALL")) {
            if (bicycle == null || !events.getBikeType().equalsIgnoreCase(bicycle.getBikeType())) {
                throw new RuntimeException("Loại xe bạn không được đăng ký vào sự kiện này");
            }
        }

        if (events.getStatus().equalsIgnoreCase("ongoing")) {
            throw new RuntimeException("Sự kiện này đang diễn ra, không thể đăng ký xe mới");
        } else if (events.getStatus().equalsIgnoreCase("completed")
                || events.getStatus().equalsIgnoreCase("cancelled")) {
            throw new RuntimeException("Sự kiện này đã kết thúc, không thể đăng ký xe mới");
        }

        EventBicycle eventBicycle = new EventBicycle();
        eventBicycle.setSeller(user);
        eventBicycle.setEvent(events);
        eventBicycle.setListing(bikeListing);
        eventBicycle.setBicycle(bicycle);
        eventBicycle.setSellerName(user.getFullName());
        eventBicycle.setStatus("Pending");
        eventBicycle.setPrice(bikeListing.getPrice());
        eventBicycle.setTitle(bikeListing.getTitle());
        eventBicycle.setBikeType(bikeListing.getBicycle().getBikeType());
        eventBicycle.setImage_url(bikeListing.getImage_url());
        eventBicycle.setCondition(bikeListing.getCondition());
        eventBicycle.setCreateDate(LocalDate.now());

        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public Bicycle createBicycle(CreatePostingRequest request) {
        Brand brand = brandRepository.findByNameIgnoreCase(request.getBrandName())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        Category category = categoryRepository.findByNameIgnoreCase(request.getCategoryName())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        Bicycle bicycle = postingMapper.toBicycle(request, brand, category);
        return bicycleRepository.save(bicycle);
    }

    public CreateEventBicycleResponse registerBicycleToEventWithoutPosting(int eventId, int bicycleId,
            EventBicycleCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Events events = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        Bicycle bicycle = bicycleRepository.findById(bicycleId)
                .orElseThrow(() -> new AppException(ErrorCode.BICYCLE_NOT_FOUND));

        if (events.getBikeType() != null && !events.getBikeType().equalsIgnoreCase("ALL")) {
            if (!events.getBikeType().equalsIgnoreCase(bicycle.getBikeType())) {
                throw new RuntimeException("Loại xe bạn không được đăng ký vào sự kiện này");
            }
        }

        if (events.getStatus().equalsIgnoreCase("ongoing")) {
            throw new RuntimeException("Sự kiện này đang diễn ra, không thể đăng ký xe mới");
        } else if (events.getStatus().equalsIgnoreCase("completed")
                || events.getStatus().equalsIgnoreCase("cancelled")) {
            throw new RuntimeException("Sự kiện này đã kết thúc, không thể đăng ký xe mới");
        }

        EventBicycle eventBicycle = new EventBicycle();
        eventBicycle.setSeller(user);
        eventBicycle.setEvent(events);
        eventBicycle.setBicycle(bicycle);
        eventBicycle.setListing(null);
        eventBicycle.setSellerName(user.getFullName());
        eventBicycle.setStatus("Pending");
        eventBicycle.setPrice(request.getPrice());
        eventBicycle.setTitle(request.getTitle());
        eventBicycle.setBikeType(bicycle.getBikeType());
        eventBicycle.setImage_url(request.getImage_url());
        eventBicycle.setCondition(request.getCondition());
        eventBicycle.setCreateDate(LocalDate.now());

        // 2. Tính phí
        double fee = postingService.calculateListingFee(request.getPrice());
        log.info("[EventBicycle] price={}, fee={}", request.getPrice(), fee);

        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
            Wallet newWallet = Wallet.builder().user(user).username(username).balance(0.0).type("User").build();
            return walletRepository.save(newWallet);
        });
        log.info("[EventBicycle] walletBalance={}, fee={}, needVNPay={}", wallet.getBalance(), fee,
                wallet.getBalance() < fee);

        // 3. Nếu VÍ KHÔNG ĐỦ TIỀN -> Lưu nháp Waiting_Payment & Trả về VNPay
        if (wallet.getBalance() < fee) {
            eventBicycle.setStatus("Waiting_Payment");
            EventBicycle savedEventBicycle = eventBicycleRepository.save(eventBicycle);

            long amountNeeded = (long) Math.ceil(fee - wallet.getBalance());
            String customReturnUrl = vnpayReturnUrl + "?eventBikeId=" + savedEventBicycle.getEventBikeId();

            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    username + "|eventfee|" + savedEventBicycle.getEventBikeId(),
                    customReturnUrl, null);

            return CreateEventBicycleResponse.builder()
                    .eventBicycle(null)
                    .paymentUrl(paymentUrl)
                    .message("Số dư không đủ. Vui lòng thanh toán " + amountNeeded + " VND.")
                    .build();
        }

        // 4. Nếu VÍ ĐỦ TIỀN -> Trừ tiền ngay và đăng bài (Pending chờ duyệt)
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance() - fee);
        systemWallet.setBalance(systemWallet.getBalance() + fee);
        walletRepository.save(wallet);
        walletRepository.save(systemWallet);

        if (fee > 0) {
            walletTransactionService.createTransaction(wallet, fee, "BicycleFee", "Phí đăng bài xe đạp");
        }

        eventBicycle.setStatus("Pending");
        EventBicycleResponse saved = eventBicycleMapper
                .toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));

        return CreateEventBicycleResponse.builder()
                .eventBicycle(saved)
                .paymentUrl(null)
                .message("Đăng bài thành công! Đã trừ " + (long) fee + " VND từ ví.")
                .build();
    }

    // --- XỬ LÝ KHI VNPAY TRẢ VỀ THÀNH CÔNG CHO EVENT BICYCLE ---
    @jakarta.transaction.Transactional
    public void confirmEventBicyclePayment(int eventBikeId, String username, double vnpayAmount) {
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));

        if (!"Waiting_Payment".equals(eventBicycle.getStatus())) {
            return; // Tránh xử lý trùng lặp
        }

        double fee = postingService.calculateListingFee(eventBicycle.getPrice());

        Wallet userWallet = walletRepository.findByUsername(username).orElseThrow();
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        // 1. Nạp tiền VNPay vào ví User
        userWallet.setBalance(userWallet.getBalance() + vnpayAmount);
        walletRepository.save(userWallet);
        walletTransactionService.createTransaction(userWallet, vnpayAmount, "Fee_TopUp",
                "Nạp tiền VNPay để trả phí đăng ký event");

        // 2. Trừ tiền ví User sang ví System
        userWallet.setBalance(userWallet.getBalance() - fee);
        systemWallet.setBalance(systemWallet.getBalance() + fee);
        walletRepository.save(userWallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(userWallet, fee, "BicycleFee",
                "Phí đăng ký xe đạp vào event #" + eventBikeId);

        // 3. Cập nhật trạng thái sang Pending chờ Admin duyệt
        eventBicycle.setStatus("Pending");
        eventBicycleRepository.save(eventBicycle);
    }

    // --- XÓA EVENT BICYCLE DRAFT NẾU USER HỦY VNPAY ---
    @jakarta.transaction.Transactional
    public void cancelEventBicyclePayment(int eventBikeId) {
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElse(null);
        if (eventBicycle != null && "Waiting_Payment".equals(eventBicycle.getStatus())) {
            Bicycle bicycle = eventBicycle.getBicycle();

            // Xóa EventBicycle trước (để giải phóng FK)
            eventBicycleRepository.delete(eventBicycle);

            // Chỉ xóa Bicycle nếu không còn bất kỳ tham chiếu nào khác
            // (EventBicycle khác hoặc BikeListing) — tránh constraint violation
            if (bicycle != null) {
                boolean usedByOtherEvent = eventBicycleRepository.existsByBicycle_BikeId(bicycle.getBikeId());
                if (!usedByOtherEvent) {
                    bicycleRepository.delete(bicycle);
                }
            }
        }
    }

    public int getEventIdByEventBikeId(int eventBikeId) {
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        return eventBicycle.getEvent().getEventId();
    }

    public EventBicycleResponse updateEventBicycle(int eventBikeId, EventBicycleUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));

        eventBicycleMapper.updateEventBicycle(eventBicycle, request);
        eventBicycle.setStatus("Pending");
        eventBicycle.setUpdateDate(LocalDate.now());
        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public EventBicycleResponse updateEventBicycleStatus(int eventBikeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        eventBicycle.setStatus("Available");
        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public EventBicycleResponse getEventBicycleById(int eventBikeId) {
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        return eventBicycleMapper.toEventBicycleResponse(eventBicycle);
    }

    public List<EventBicycle> getAllEventBicycles() {
        return eventBicycleRepository.findAll();
    }

    @Transactional
    public String deleteEventBicycle(int eventBikeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
//        if(depositRepository.existsByEventBicycle(eventBicycle)) {
//            depositRepository.deleteByEventBicycle(eventBicycle);
//        }
        eventBicycleRepository.delete(eventBicycle);
        return "Event Bicycle deleted successfully";
    }
}