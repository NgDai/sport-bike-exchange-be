package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.EventBicycleCreationRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventBicycleService {
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
    private WalletTransactionService walletTransactionService;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private PostingService postingService;

    public EventBicycleResponse registerBicycleToEvent(int eventId, int listingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Events events = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        BikeListing bikeListing = bikeListingRepository.findById(listingId).orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
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

    public EventBicycleResponse registerBicycleToEventWithoutPosting(int eventId, int bicycleId, EventBicycleCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Events events = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        Bicycle bicycle = bicycleRepository.findById(bicycleId).orElseThrow(() -> new AppException(ErrorCode.BICYCLE_NOT_FOUND));

        if (events.getBikeType() != null && !events.getBikeType().equalsIgnoreCase("ALL")) {
            if (!events.getBikeType().equalsIgnoreCase(bicycle.getBikeType())) {
                throw new RuntimeException("Loại xe bạn không được đăng ký vào sự kiện này");
            }
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
        eventBicycle.setCreateDate(LocalDate.now());

//        // 2. Tính phí
//        double fee = postingService.calculateListingFee(request.getPrice());
//
//        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
//            Wallet newWallet = Wallet.builder().user(user).username(username).balance(0.0).type("User").build();
//            return walletRepository.save(newWallet);
//        });
//
//        // 3. Nếu VÍ KHÔNG ĐỦ TIỀN -> Lưu nháp Waiting_Payment & Trả về VNPay
//        if (wallet.getBalance() < fee) {
//            eventBicycle.setStatus("Waiting_Payment");
//            EventBicycle savedEventBicycle = eventBicycleRepository.save(eventBicycle);
//
//            long amountNeeded = (long) Math.ceil(listingFee - wallet.getBalance());
//            String customReturnUrl = vnpayReturnUrl + "?listingId=" + savedListing.getListingId();
//
//            String paymentUrl = vnPayService.createOrder(
//                    amountNeeded,
//                    username + "|fee|" + savedListing.getListingId(),
//                    customReturnUrl, null
//            );
//
//            return CreatePostingResponse.builder()
//                    .listing(null)
//                    .paymentUrl(paymentUrl)
//                    .message("Số dư không đủ. Vui lòng thanh toán " + amountNeeded + " VND.")
//                    .build();
//        }
//
//        // 4. Nếu VÍ ĐỦ TIỀN -> Trừ tiền ngay và đăng bài (Pending chờ duyệt)
//        Wallet systemWallet = walletRepository.findByUsername("System")
//                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
//
//        wallet.setBalance(wallet.getBalance() - listingFee);
//        systemWallet.setBalance(systemWallet.getBalance() + listingFee);
//        walletRepository.save(wallet);
//        walletRepository.save(systemWallet);
//
//        if (listingFee > 0) {
//            walletTransactionService.createTransaction(wallet, listingFee, "ListingFee", "Phí đăng bài xe đạp");
//        }

        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public EventBicycleResponse updateEventBicycle(int eventBikeId, EventBicycleUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));

        eventBicycleMapper.updateEventBicycle(eventBicycle, request);
        eventBicycle.setStatus("Pending");
        eventBicycle.setCreateDate(LocalDate.now());
        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public EventBicycleResponse updateEventBicycleStatus(int eventBikeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        eventBicycle.setStatus("Available");
        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public EventBicycleResponse getEventBicycleById(int eventBikeId) {
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        return eventBicycleMapper.toEventBicycleResponse(eventBicycle);
    }

    public List<EventBicycle> getAllEventBicycles() {
        return eventBicycleRepository.findAll();
    }

    public String deleteEventBicycle(int eventBikeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        eventBicycleRepository.delete(eventBicycle);
        return "Event Bicycle deleted successfully";
    }
}