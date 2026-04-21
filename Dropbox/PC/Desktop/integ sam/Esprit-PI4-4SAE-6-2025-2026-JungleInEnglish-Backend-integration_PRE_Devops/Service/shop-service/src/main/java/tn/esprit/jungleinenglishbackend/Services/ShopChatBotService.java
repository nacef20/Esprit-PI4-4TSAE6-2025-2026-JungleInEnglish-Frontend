package tn.esprit.jungleinenglishbackend.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import tn.esprit.jungleinenglishbackend.Models.Product;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopChatBotService {

    private final ChatModel chatModel;
    private final ProductService productService;

    public String chatWithStoreContext(String userMessage) {
        // Fetch all active products (we use getAllProducts and filter isActive if needed)
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .collect(Collectors.toList());

        // Construct the catalog context
        String catalogContext;
        if (products.isEmpty()) {
            catalogContext = "Currently, there are no active products in the shop.";
        } else {
            catalogContext = products.stream()
                    .map(p -> String.format("- %s (Category: %s, Price: %.2f$, Stock: %d, Discount: %d%%)", 
                            p.getTitle(), p.getCategory(), p.getPrice(), p.getStock(), p.getDiscount()))
                    .collect(Collectors.joining("\n"));
        }

        // Build the system prompt
        String prompt = String.format(
                "You are an intelligent shopping assistant for 'Jungle In English' store. " +
                "Use the following product catalog to answer the customer's questions. " +
                "Be helpful, concise, and recommend products that match their needs based ONLY on the catalog below.\n\n" +
                "--- Product Catalog ---\n%s\n-----------------------\n\n" +
                "Customer: %s",
                catalogContext, userMessage
        );

        return chatModel.call(prompt);
    }
}
