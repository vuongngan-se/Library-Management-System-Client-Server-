package com.library.server.service;

import com.library.common.model.Book;
import com.library.common.model.Category;
import com.library.server.dao.InMemoryStore;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Trợ lý Chatbot AI Thư viện (SmartLib AI Assistant):
 * Phân tích ngôn ngữ tự nhiên (NLP) và tra cứu cơ sở dữ liệu thời gian thực
 * để giải đáp về sách, tình trạng tồn kho, tác giả, thể loại, gợi ý sách và quy chế thư viện.
 */
public class AiChatbotService {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Chuẩn hóa chuỗi tiếng Việt thành không dấu và chữ thường để so khớp linh hoạt.
     */
    public static String normalize(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        return withoutAccents.toLowerCase().replace('đ', 'd').replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    /**
     * Xử lý câu hỏi tự nhiên của người dùng và trả về câu trả lời thông minh.
     */
    public static String answerQuery(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return "👋 Xin chào! Tôi là Trợ lý AI của Thư viện. Bạn có thể hỏi tôi về bất kỳ đầu sách nào, kiểm tra số lượng còn khả dụng hoặc nhờ tôi gợi ý sách hay nhé!";
        }

        String raw = userQuery.trim();
        String norm = normalize(raw);

        List<Book> allBooks = InMemoryStore.getAllBooks();
        List<Category> allCategories = InMemoryStore.getAllCategories();

        // 1. Chào hỏi / Giới thiệu / Trợ giúp
        if (norm.matches("^(chao|xin chao|hello|hi|helo|alo|hai|tro ly|bot|ai).*") ||
            norm.contains("ban la ai") || norm.contains("lam duoc gi") || norm.contains("huong dan") || norm.equals("help")) {
            return "🤖 **Xin chào bạn! Tôi là Trợ Lý AI của Thư viện SmartLib.**\n\n" +
                    "Tôi có thể hỗ trợ bạn tra cứu trực tiếp từ Cơ sở dữ liệu thư viện:\n" +
                    "• 🔍 **Tra cứu sách**: *'Thư viện có sách Clean Code không?'*\n" +
                    "• 📦 **Kiểm tra tồn kho**: *'Sách Lập trình Java Socket còn bao nhiêu cuốn?'*\n" +
                    "• ✍️ **Tìm theo tác giả**: *'Tác giả Robert C. Martin có những sách nào?'*\n" +
                    "• 🏷️ **Tìm theo thể loại**: *'Có sách nào về Trí tuệ nhân tạo (AI) không?'*\n" +
                    "• 🏆 **Sách phổ biến**: *'Những cuốn sách nào đang được mượn nhiều nhất?'*\n" +
                    "• 📜 **Quy chế thư viện**: *'Thời hạn mượn và phí phạt quá hạn là bao nhiêu?'*\n\n" +
                    "👉 Bạn cứ gõ câu hỏi tự nhiên, tôi sẽ tra cứu và trả lời ngay!";
        }

        // 2. Hỏi về quy định mượn trả, tiền phạt, gia hạn
        if (norm.contains("quy dinh") || norm.contains("muon nhu the nao") || norm.contains("cach muon") ||
            norm.contains("tien phat") || norm.contains("qua han") || norm.contains("gia han") || norm.contains("bao nhieu ngay")) {
            return "📜 **QUY ĐỊNH MƯỢN TRẢ & GIA HẠN CỦA THƯ VIỆN:**\n\n" +
                    "1. **Thời hạn mượn**: Mỗi lần mượn tối đa **14 ngày**.\n" +
                    "2. **Gia hạn trực tuyến**: Bạn có thể vào tab **'Sách Của Tôi'** trên ứng dụng để tự gia hạn thêm **+7 ngày** (áp dụng khi sách chưa bị quá hạn).\n" +
                    "3. **Phí phạt quá hạn**: Nếu trả sách trễ hạn, hệ thống tính phí **5.000 VNĐ / ngày trễ**.\n" +
                    "4. **Đặt trước sách (Reservation)**: Nếu sách bạn cần đã hết bản khả dụng (`0 bản`), bạn có thể gửi yêu cầu Đặt trước để nhận thông báo ưu tiên khi có sách trả về kho.";
        }

        // 3. Top sách mượn nhiều nhất / Sách hot
        if (norm.contains("top sach") || norm.contains("muon nhieu nhat") || norm.contains("sach hot") ||
            norm.contains("pho bien") || norm.contains("nhieu nguoi doc")) {
            Map<String, Integer> topBooks = InMemoryStore.getTopBorrowedBooks();
            StringBuilder sb = new StringBuilder("🏆 **TOP SÁCH ĐƯỢC MƯỢN NHIỀU NHẤT TRONG THƯ VIỆN:**\n\n");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : topBooks.entrySet()) {
                if (rank > 5) break;
                sb.append(rank).append(". 📖 **").append(entry.getKey()).append("** — ")
                  .append(entry.getValue()).append(" lượt mượn\n");
                rank++;
            }
            sb.append("\n💡 Bạn có thể tìm đọc ngay các đầu sách nổi bật trên bằng cách gõ tên sách để tôi kiểm tra tình trạng còn nhé!");
            return sb.toString();
        }

        // 4. Gợi ý sách hay / Nên đọc gì
        if (norm.contains("goi y") || norm.contains("sach hay") || norm.contains("nen doc") || norm.contains("recommend")) {
            List<Book> candidates = allBooks.stream()
                    .filter(b -> b.getAvailableQuantity() > 0)
                    .limit(5)
                    .collect(Collectors.toList());
            StringBuilder sb = new StringBuilder("✨ **GỢI Ý CÁC ĐẦU SÁCH HAY ĐANG CÓ SẴN TRONG KHO:**\n\n");
            for (Book b : candidates) {
                sb.append("• 📘 **").append(b.getTitle()).append("**\n")
                  .append("   - Tác giả: ").append(b.getAuthor()).append("\n")
                  .append("   - Thể loại: ").append(b.getCategoryName()).append("\n")
                  .append("   - Tình trạng: 🟢 Còn ").append(b.getAvailableQuantity()).append("/").append(b.getQuantity()).append(" cuốn khả dụng\n\n");
            }
            sb.append("👉 Bạn hãy đến quầy thư viện hoặc đăng ký mượn ngay trên ứng dụng nhé!");
            return sb.toString();
        }

        // 5. Tìm kiếm theo Tác giả cụ thể
        String authorKeyword = extractAuthorKeyword(norm);
        if (authorKeyword != null && !authorKeyword.isEmpty()) {
            List<Book> byAuthor = allBooks.stream()
                    .filter(b -> normalize(b.getAuthor()).contains(authorKeyword))
                    .collect(Collectors.toList());
            if (!byAuthor.isEmpty()) {
                StringBuilder sb = new StringBuilder("✍️ **CÁC ĐẦU SÁCH CỦA TÁC GIẢ '")
                        .append(byAuthor.get(0).getAuthor()).append("' TRONG CƠ SỞ DỮ LIỆU:**\n\n");
                for (Book b : byAuthor) {
                    formatBookInfo(sb, b);
                }
                return sb.toString();
            }
        }

        // 6. Tìm kiếm theo Thể loại
        String categoryKeyword = extractCategoryKeyword(norm);
        if (categoryKeyword != null && !categoryKeyword.isEmpty()) {
            List<Book> byCategory = allBooks.stream()
                    .filter(b -> normalize(b.getCategoryName()).contains(categoryKeyword))
                    .collect(Collectors.toList());
            if (!byCategory.isEmpty()) {
                StringBuilder sb = new StringBuilder("🏷️ **DANH MỤC SÁCH THUỘC THỂ LOẠI '")
                        .append(byCategory.get(0).getCategoryName()).append("' (")
                        .append(byCategory.size()).append(" cuốn):**\n\n");
                for (Book b : byCategory.stream().limit(6).collect(Collectors.toList())) {
                    formatBookInfo(sb, b);
                }
                if (byCategory.size() > 6) {
                    sb.append("*(Và còn ").append(byCategory.size() - 6).append(" cuốn khác thuộc thể loại này trong kho...)*\n");
                }
                return sb.toString();
            }
        }

        // 7. Tìm kiếm theo Tiêu đề / Nội dung từ khóa (Full-text & Substring Match)
        String cleanedSearchTerm = cleanSearchTerms(norm);
        List<Book> matchedBooks = findMatchingBooks(allBooks, cleanedSearchTerm, norm);

        if (!matchedBooks.isEmpty()) {
            boolean isAvailabilityCheck = norm.contains("con khong") || norm.contains("con bao nhieu") ||
                    norm.contains("ton kho") || norm.contains("con sach");

            StringBuilder sb = new StringBuilder();
            if (matchedBooks.size() == 1) {
                Book b = matchedBooks.get(0);
                if (isAvailabilityCheck) {
                    sb.append("📦 **KIỂM TRA TỒN KHO SÁCH '").append(b.getTitle()).append("':**\n\n");
                } else {
                    sb.append("🔍 **THÔNG TIN SÁCH TÌM THẤY TRONG CSDL:**\n\n");
                }
                formatBookInfo(sb, b);
            } else {
                sb.append("🔍 **TÌM THẤY ").append(matchedBooks.size()).append(" ĐẦU SÁCH PHÙ HỢP TRONG THƯ VIỆN:**\n\n");
                for (Book b : matchedBooks.stream().limit(5).collect(Collectors.toList())) {
                    formatBookInfo(sb, b);
                }
                if (matchedBooks.size() > 5) {
                    sb.append("*(Còn ").append(matchedBooks.size() - 5).append(" sách khác phù hợp, bạn hãy gõ tên cụ thể hơn để lọc nhé)*\n");
                }
            }
            return sb.toString();
        }

        // 8. Không tìm thấy kết quả phù hợp
        return "🤔 **Rất tiếc, tôi chưa tìm thấy đầu sách nào phù hợp với yêu cầu: '" + raw + "' trong cơ sở dữ liệu thư viện.**\n\n" +
                "💡 **Gợi ý tra cứu:**\n" +
                "• Thử tìm theo từ khóa ngắn hơn (Ví dụ: *'Java'*, *'Clean Code'*, *'Python'*, *'AI'*).\n" +
                "• Hoặc tra cứu theo chủ đề: *'Sách công nghệ thông tin'*, *'Sách văn học'*, *'Sách thiếu nhi'*.\n" +
                "• Gõ *'Gợi ý sách'* để xem các cuốn đang sẵn sàng cho mượn!";
    }

    private static void formatBookInfo(StringBuilder sb, Book b) {
        sb.append("📖 **").append(b.getTitle()).append("**\n")
          .append("   • Tác giả: ").append(b.getAuthor()).append("\n")
          .append("   • Thể loại: ").append(b.getCategoryName() != null ? b.getCategoryName() : "Khác")
          .append(" | Năm XB: ").append(b.getPublishYear()).append(" (").append(b.getPublisher()).append(")\n")
          .append("   • ISBN: `").append(b.getIsbn() != null ? b.getIsbn() : "N/A").append("` (Mã sách: #").append(b.getBookId()).append(")\n");

        if (b.getAvailableQuantity() > 0) {
            sb.append("   • Tình trạng: 🟢 **CÒN SÁCH** (Khả dụng: **")
              .append(b.getAvailableQuantity()).append("/").append(b.getQuantity()).append("** cuốn sẵn sàng cho mượn)\n\n");
        } else {
            sb.append("   • Tình trạng: 🔴 **TẠM HẾT SÁCH** (0/").append(b.getQuantity())
              .append(" cuốn. Bạn có thể sử dụng chức năng Đặt Trước Sách trên hệ thống để được ưu tiên khi có sách trả về)\n\n");
        }
    }

    private static List<Book> findMatchingBooks(List<Book> allBooks, String cleanedTerm, String originalNorm) {
        if (cleanedTerm.isEmpty()) cleanedTerm = originalNorm;

        // Ưu tiên 1: So khớp tiêu đề chính xác hoặc chứa toàn bộ cụm từ
        String finalTerm = cleanedTerm;
        List<Book> exactMatches = allBooks.stream()
                .filter(b -> normalize(b.getTitle()).contains(finalTerm))
                .collect(Collectors.toList());
        if (!exactMatches.isEmpty()) return exactMatches;

        // Ưu tiên 2: So khớp theo tác giả
        List<Book> authorMatches = allBooks.stream()
                .filter(b -> normalize(b.getAuthor()).contains(finalTerm))
                .collect(Collectors.toList());
        if (!authorMatches.isEmpty()) return authorMatches;

        // Ưu tiên 3: Tách từ khóa quan trọng và đếm điểm số trùng khớp
        String[] words = finalTerm.split("\\s+");
        List<String> validWords = Arrays.stream(words)
                .filter(w -> w.length() > 1 && !isStopWord(w))
                .collect(Collectors.toList());

        if (validWords.isEmpty()) return Collections.emptyList();

        Map<Book, Integer> scored = new HashMap<>();
        for (Book b : allBooks) {
            String normTitle = normalize(b.getTitle());
            String normAuthor = normalize(b.getAuthor());
            String normCat = normalize(b.getCategoryName());

            Set<String> titleWords = new HashSet<>(Arrays.asList(normTitle.split("\\s+")));
            Set<String> authorWords = new HashSet<>(Arrays.asList(normAuthor.split("\\s+")));
            Set<String> catWords = normCat != null ? new HashSet<>(Arrays.asList(normCat.split("\\s+"))) : Collections.emptySet();

            int matchedWordCount = 0;
            int score = 0;
            for (String w : validWords) {
                boolean hit = false;
                if (titleWords.contains(w)) {
                    score += 5;
                    hit = true;
                } else if (authorWords.contains(w)) {
                    score += 3;
                    hit = true;
                } else if (catWords.contains(w)) {
                    score += 1;
                    hit = true;
                }
                if (hit) matchedWordCount++;
            }

            // Yêu cầu điểm số tối thiểu và tỷ lệ trùng khớp từ khóa hợp lý
            double ratio = (double) matchedWordCount / validWords.size();
            boolean validMatch = false;
            if (validWords.size() == 1 && score >= 5) {
                validMatch = true;
            } else if (validWords.size() == 2 && matchedWordCount == 2) {
                validMatch = true;
            } else if (validWords.size() > 2 && ratio >= 0.6) {
                validMatch = true;
            }

            if (validMatch) {
                scored.put(b, score);
            }
        }

        return scored.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static String extractAuthorKeyword(String norm) {
        String[] prefixes = {"tac gia", "viet boi", "cua ong", "cua ba", "tac pham cua"};
        for (String p : prefixes) {
            int idx = norm.indexOf(p);
            if (idx != -1) {
                return norm.substring(idx + p.length()).trim();
            }
        }
        return null;
    }

    private static String extractCategoryKeyword(String norm) {
        if (norm.contains("lap trinh") || norm.contains("code") || norm.contains("cong nghe") || norm.contains("cntt")) {
            return "cong nghe";
        }
        if (norm.contains("tri tue nhan tao") || norm.contains("ai") || norm.contains("machine learning") || norm.contains("du lieu")) {
            return "tri tue nhan tao";
        }
        if (norm.contains("mang may tinh") || norm.contains("an toan thong tin") || norm.contains("bao mat")) {
            return "mang";
        }
        if (norm.contains("van hoc") || norm.contains("tieu thuyet")) {
            return "van hoc";
        }
        if (norm.contains("kinh te") || norm.contains("kinh doanh") || norm.contains("tai chinh")) {
            return "kinh te";
        }
        if (norm.contains("lich su") || norm.contains("dia ly")) {
            return "lich su";
        }
        if (norm.contains("thieu nhi") || norm.contains("truyen tranh")) {
            return "thieu nhi";
        }
        if (norm.contains("tam ly") || norm.contains("ky nang")) {
            return "tam ly";
        }
        if (norm.contains("y hoc") || norm.contains("suc khoe")) {
            return "y hoc";
        }
        if (norm.contains("ngoai ngu") || norm.contains("tieng anh")) {
            return "ngoai ngu";
        }
        return null;
    }

    private static String cleanSearchTerms(String norm) {
        return norm.replaceAll("\\b(co|sach|nao|ve|khong|tim|cho|toi|biet|thong|tin|cuon|dau|sach|tac|pham|hien|tai|trong|thu|vien|con|bao|nhieu|quyen|ban|hay)\\b", " ")
                   .replaceAll("\\s+", " ").trim();
    }

    private static boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
                "co", "sach", "nao", "ve", "khong", "tim", "cho", "toi", "biet", "thong",
                "tin", "cuon", "dau", "tac", "pham", "trong", "thu", "vien", "con", "bao", "nhieu"
        );
        return stopWords.contains(word);
    }
}
