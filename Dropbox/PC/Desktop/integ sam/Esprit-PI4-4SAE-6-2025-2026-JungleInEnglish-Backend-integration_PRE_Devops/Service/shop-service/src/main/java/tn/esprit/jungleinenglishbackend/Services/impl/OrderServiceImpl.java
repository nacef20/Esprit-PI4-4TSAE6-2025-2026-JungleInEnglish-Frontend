package tn.esprit.jungleinenglishbackend.Services.impl;

import tn.esprit.jungleinenglishbackend.Models.*;
import tn.esprit.jungleinenglishbackend.Reposotories.CartRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductOrderRepo;
import tn.esprit.jungleinenglishbackend.Reposotories.ProductRepo;
import tn.esprit.jungleinenglishbackend.Services.OrderService;
import tn.esprit.jungleinenglishbackend.exceptions.InsufficientStockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductOrderRepo orderRepository;

    @Autowired
    private CartRepo cartRepository;

    @Autowired
    private ProductRepo productRepository;

    @Override
    @Transactional
    public void saveOrder(String userId, OrderDTO orderRequest) throws Exception {
        try {
            List<Cart> carts = cartRepository.findByUserId(userId);
            if (carts.isEmpty()) {
                throw new Exception("Cart is empty for user id: " + userId);
            }

            for (Cart cart : carts) {
                Product product = cart.getProduct();

                if (product.getStock() < cart.getQuantity()) {
                    throw new InsufficientStockException(product.getId(), cart.getQuantity(), product.getStock());
                }
                product.setStock(product.getStock() - cart.getQuantity());
                productRepository.save(product);

                ProductOrder order = new ProductOrder();

                order.setOrderId(UUID.randomUUID().toString());
                order.setOrderDate(LocalDate.now());

                order.setProduct(product);
                order.setPrice(cart.getProduct().getDiscountPrice());

                order.setQuantity(cart.getQuantity());
                order.setUserId(userId);

                order.setStatus(OrderStatus.IN_PROGRESS.getName());
                order.setPaymentType(orderRequest.getPaymentType());

                OrderAdress address = new OrderAdress();
                address.setFirstName(orderRequest.getFirstName());
                address.setLastName(orderRequest.getLastName());
                address.setEmail(orderRequest.getEmail() != null
                        ? orderRequest.getEmail()
                        : "no-email@example.com");
                address.setTelephone(orderRequest.getTelephone());
                address.setAddress(orderRequest.getAddress());
                address.setCity(orderRequest.getCity());
                address.setState(orderRequest.getState());
                address.setPostalCode(orderRequest.getPostalCode());

                order.setOrderAddress(address);

                orderRepository.save(order);
            }

            cartRepository.deleteByUserId(userId);

        } catch (Exception e) {
            System.err.println("Error in saveOrder: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<ProductOrder> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public ProductOrder updateOrderStatus(Integer id, String status) {
        Optional<ProductOrder> findById = orderRepository.findById(id);
        if (findById.isPresent()) {
            ProductOrder productOrder = findById.get();
            productOrder.setStatus(status);
            return orderRepository.save(productOrder);
        }
        return null;
    }

    @Override
    public List<ProductOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return orderRepository.findAll(pageable);
    }

    @Override
    public ProductOrder getOrdersByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    @Override
    public ProductOrder getOrderById(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ProductOrder updateOrder(Integer id, ProductOrder orderData) {
        Optional<ProductOrder> findById = orderRepository.findById(id);
        if (findById.isPresent()) {
            ProductOrder existingOrder = findById.get();

            existingOrder.setQuantity(orderData.getQuantity());

            if (orderData.getStatus() != null) {
                existingOrder.setStatus(orderData.getStatus());
            }

            if (orderData.getOrderAddress() != null && existingOrder.getOrderAddress() != null) {
                OrderAdress existingAddr = existingOrder.getOrderAddress();
                OrderAdress newAddr = orderData.getOrderAddress();

                existingAddr.setFirstName(newAddr.getFirstName());
                existingAddr.setLastName(newAddr.getLastName());
                existingAddr.setEmail(newAddr.getEmail());
                existingAddr.setTelephone(newAddr.getTelephone());
                existingAddr.setAddress(newAddr.getAddress());
                existingAddr.setCity(newAddr.getCity());
                existingAddr.setState(newAddr.getState());
                existingAddr.setPostalCode(newAddr.getPostalCode());
            }

            return orderRepository.save(existingOrder);
        }
        return null;
    }

    @Override
    public java.math.BigDecimal getTotalMoneyForCurrentUser() {
        String userId = tn.esprit.jungleinenglishbackend.security.SecurityUtils.getCurrentUserId();
        Double sum = orderRepository.sumTotalAmountByUserId(userId);
        return java.math.BigDecimal.valueOf(sum).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public java.math.BigDecimal getTotalMoneyForAllUsers() {
        Double sum = orderRepository.sumTotalAmountAllUsers();
        return java.math.BigDecimal.valueOf(sum).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public LocationDTO getLocationDTO(Integer orderId) {
        ProductOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new tn.esprit.jungleinenglishbackend.exceptions.OrderNotFoundException(
                        "Order not found with id: " + orderId));

        if (order.getLatitude() == null || order.getLongitude() == null) {
            String searchQuery = "";
            if (order.getOrderAddress() != null) {
                String address = order.getOrderAddress().getAddress() != null
                        ? order.getOrderAddress().getAddress() + " "
                        : "";
                String city = order.getOrderAddress().getCity() != null ? order.getOrderAddress().getCity() : "";
                String state = order.getOrderAddress().getState() != null ? " " + order.getOrderAddress().getState()
                        : "";
                searchQuery = address + city + state;
            }

            if (!searchQuery.trim().isEmpty()) {
                try {
                    org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                    String encodedQuery = java.net.URLEncoder.encode(searchQuery.trim(),
                            java.nio.charset.StandardCharsets.UTF_8);
                    String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery;

                    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.set("User-Agent", "JungleInEnglishBackend/1.0");
                    org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(
                            headers);

                    org.springframework.http.ResponseEntity<java.util.List> response = restTemplate.exchange(url,
                            org.springframework.http.HttpMethod.GET, entity, java.util.List.class);

                    if (response.getBody() != null && !response.getBody().isEmpty()) {
                        java.util.Map<String, Object> firstResult = (java.util.Map<String, Object>) response.getBody()
                                .get(0);
                        order.setLatitude(Double.parseDouble((String) firstResult.get("lat")));
                        order.setLongitude(Double.parseDouble((String) firstResult.get("lon")));
                        order.setDeliveryAddress((String) firstResult.get("display_name"));
                        orderRepository.save(order);
                    }
                } catch (Exception e) {
                    System.err.println("Forward Geocoding failed: " + e.getMessage());
                }
            }
        }

        return new LocationDTO(order.getLatitude(), order.getLongitude(), order.getDeliveryAddress());
    }

    @Override
    @Transactional
    public void updateOrderLocation(Integer orderId, Double latitude, Double longitude) {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new tn.esprit.jungleinenglishbackend.exceptions.InvalidLocationException(
                    "Invalid latitude: " + latitude);
        }
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new tn.esprit.jungleinenglishbackend.exceptions.InvalidLocationException(
                    "Invalid longitude: " + longitude);
        }

        ProductOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new tn.esprit.jungleinenglishbackend.exceptions.OrderNotFoundException(
                        "Order not found with id: " + orderId));

        order.setLatitude(latitude);
        order.setLongitude(longitude);

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon="
                    + longitude;

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "JungleInEnglishBackend/1.0");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.exchange(url,
                    org.springframework.http.HttpMethod.GET, entity, java.util.Map.class);

            if (response.getBody() != null && response.getBody().containsKey("display_name")) {
                order.setDeliveryAddress((String) response.getBody().get("display_name"));
            } else {
                order.setDeliveryAddress("Unknown Address");
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch address from Nominatim: " + e.getMessage());
            order.setDeliveryAddress("Address fetch failed");
        }

        orderRepository.save(order);
    }
}
