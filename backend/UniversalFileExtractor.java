
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

@WebServlet("/UniversalFileExtractor")
public class UniversalFileExtractor extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static String extract(String filePath) {
        if (filePath == null || filePath.isEmpty()) return "";
        File file = new File(filePath);
        if (!file.exists()) return "";

        String lower = filePath.toLowerCase();

        try {
            if (lower.endsWith(".pdf")) return extractPDF(file);
            if (lower.endsWith(".docx")) return extractDOCX(file);
            if (lower.endsWith(".pptx")) return extractPPTX(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String extractPDF(File file) throws Exception {
        try (PDDocument doc = Loader.loadPDF(file)) {  // PDFBox 3.x Loader
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private static String extractDOCX(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    private static String extractPPTX(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape sh : slide.getShapes()) {
                    if (sh instanceof XSLFTextShape) {
                        sb.append(((XSLFTextShape) sh).getText()).append("\n");
                    }
                }
            }
            return sb.toString();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().append("UniversalFileExtractor is running. Use POST to extract files.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String filePath = request.getParameter("filePath");  // send file path in POST param
        if (filePath == null || filePath.isEmpty()) {
            out.println("No file path provided.");
            return;
        }

        String text = extract(filePath);
        if (text.isEmpty()) {
            out.println("File not found or empty: " + filePath);
        } else {
            out.println(text);
        }
    }
}
