package tn.esprit.jungleinenglishbackend.Services;

import tn.esprit.jungleinenglishbackend.Models.OrderDTO;
import tn.esprit.jungleinenglishbackend.Models.ProductOrder;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    void saveOrder(String userId, OrderDTO orderRequest) throws Exception;

    List<ProductOrder> getOrdersByUser(String userId);

    ProductOrder updateOrderStatus(Integer id, String status);

    List<ProductOrder> getAllOrders();

    ProductOrder getOrdersByOrderId(String orderId);

    Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize);

    void deleteOrder(Integer id);

    ProductOrder getOrderById(Integer id);

    ProductOrder updateOrder(Integer id, ProductOrder order);

    java.math.BigDecimal getTotalMoneyForCurrentUser();

    java.math.BigDecimal getTotalMoneyForAllUsers();

    tn.esprit.jungleinenglishbackend.Models.LocationDTO getLocationDTO(Integer orderId);

    void updateOrderLocation(Integer orderId, Double latitude, Double longitude);
}
