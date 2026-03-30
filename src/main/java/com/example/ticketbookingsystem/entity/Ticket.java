package com.example.ticketbookingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "ticket_type", nullable = false)
    private String ticketType;

    @Column(nullable = false)
    private BigDecimal price; // Luôn dùng BigDecimal cho tiền tệ thay vì Double

    @Column(nullable = false)
    private Integer stock;

    // CHIÊU THỨC "ĐÓNG BĂNG" TRANH CHẤP DỮ LIỆU
    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Hàm nghiệp vụ: Giảm tồn kho an toàn
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new RuntimeException("Số lượng vé không đủ!");
        }
        this.stock -= quantity;
    }
}
