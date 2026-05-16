package com.system.orders.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import com.system.orders.dto.OrderDetailsDto;
import com.system.orders.dto.OrderItemDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class PdfActService {

    private final OrderService orderService;

    public byte[] generateRepairAct(Long orderId) {

        OrderDetailsDto order = orderService.getOrderWithItems(orderId);

        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 40, 40, 40, 40);

            PdfWriter.getInstance(document, out);

            document.open();

            InputStream fontStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("fonts/DejaVuSans.ttf");

            if (fontStream == null) {
                throw new RuntimeException("Font not found");
            }

            Path tempFont = Files.createTempFile("dejavu", ".ttf");

            Files.copy(fontStream, tempFont, StandardCopyOption.REPLACE_EXISTING);

            BaseFont baseFont = BaseFont.createFont(
                    tempFont.toAbsolutePath().toString(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font normalFont = new Font(baseFont, 12);
            Font boldFont = new Font(baseFont, 12, Font.BOLD);

            // Заголовок
            Paragraph title = new Paragraph(
                    "Акт выполненных работ",
                    titleFont
            );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(new Paragraph(" ", normalFont));

            // Информация о заказе
            document.add(new Paragraph(
                    "Номер заказа: " + order.getOrderId(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Статус: " + order.getStatus(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Дата создания: " + order.getCreatedAt(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Дата завершения: " + order.getCompletedAt(),
                    normalFont
            ));

            document.add(new Paragraph(" ", normalFont));

            // Устройство
            document.add(new Paragraph(
                    "Модель устройства: " + order.getDeviceModel(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Серийный номер: " + order.getDeviceSerial(),
                    normalFont
            ));

            document.add(new Paragraph(" ", normalFont));

            // Таблица
            PdfPTable table = new PdfPTable(5);

            table.setWidthPercentage(100);

            table.setWidths(new float[]{4, 2, 1, 2, 2});

            addHeader(table, "Наименование", boldFont);
            addHeader(table, "Категория", boldFont);
            addHeader(table, "Кол-во", boldFont);
            addHeader(table, "Цена", boldFont);
            addHeader(table, "Сумма", boldFont);

            BigDecimal total = BigDecimal.ZERO;

            for (OrderItemDto item : order.getItems()) {

                BigDecimal rowSum =
                        item.getSellPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()));

                total = total.add(rowSum);

                addCell(table, item.getName(), normalFont);
                addCell(table, item.getCategory(), normalFont);
                addCell(table, item.getQuantity().toString(), normalFont);
                addCell(table, item.getSellPrice().toString(), normalFont);
                addCell(table, rowSum.toString(), normalFont);
            }

            document.add(table);

            document.add(new Paragraph(" ", normalFont));

            document.add(new Paragraph(
                    "Итоговая стоимость: " + total + " руб.",
                    boldFont
            ));

            document.add(new Paragraph(" ", normalFont));

            document.add(new Paragraph(
                    "Результат диагностики:",
                    boldFont
            ));

            document.add(new Paragraph(
                    order.getDiagnosticResult() != null
                            ? order.getDiagnosticResult()
                            : "Не указано",
                    normalFont
            ));

            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph(" ", normalFont));

            document.add(new Paragraph(
                    "Подпись мастера: ____________________",
                    normalFont
            ));

            document.add(new Paragraph(" ", normalFont));

            document.add(new Paragraph(
                    "Подпись клиента: ____________________",
                    normalFont
            ));

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate pdf", e);
        }
    }

    private void addHeader(PdfPTable table, String text, Font font) {

        PdfPCell cell = new PdfPCell();

        cell.setPhrase(new Phrase(text, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {

        PdfPCell cell = new PdfPCell(
                new Phrase(text, font)
        );

        table.addCell(cell);
    }
}