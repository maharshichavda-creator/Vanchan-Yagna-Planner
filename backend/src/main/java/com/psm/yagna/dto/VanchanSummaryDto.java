package com.psm.yagna.dto;

public record VanchanSummaryDto(int pagesPerBooking, int totalBooks, int totalPages, int allocatedPages, int remainingPages,
                                String nextBookTitle, Integer nextStartPage) {
}