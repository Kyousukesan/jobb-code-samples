package main

import "sort"

func twoSum(nums []int, target int) []int {
	numMap := make(map[int]int)
	for i, num := range nums {
		n := target - num
		if key, ok := numMap[n]; ok {
			return []int{key, i}
		}
		numMap[num] = i
	}
	return []int{}
}

func containsDuplicate(nums []int) bool {
	numMap := make(map[int]int)
	for i, num := range nums {
		if _, ok := numMap[num]; ok {
			return true
		}
		numMap[num] = i
	}
	return false
}

func intersect(nums1 []int, nums2 []int) []int {
	numMap := make(map[int]int)
	for _, num := range nums1 {
		numMap[num]++
	}
	result := make([]int, 0)
	for _, num := range nums2 {
		if count, ok := numMap[num]; ok && count > 0 {
			result = append(result, num)
			numMap[num]--
		}
	}

	return result
}

func threeSum(nums []int) [][]int {
	result := make([][]int, 0)
	max := len(nums)
	if max < 3 {
		return result
	}
	sort.Ints(nums)
	if nums[0] > 0 || nums[max-1] < 0 {
		return result
	}

	for i := 0; i < max-2; i++ {
		if i > 0 && nums[i] == nums[i-1] {
			continue
		}
		j := i + 1
		k := max - 1
		for j < k {
			sum := nums[i] + nums[j] + nums[k]
			if sum == 0 {
				result = append(result, []int{nums[i], nums[j], nums[k]})
				for j < k && nums[j] == nums[j+1] {
					j++
				}
				for j < k && nums[k] == nums[k-1] {
					k--
				}
				j++
				k--
			} else if sum < 0 {
				j++
			} else {
				k--
			}
		}
	}
	return result
}

func lengthOfLongestSubstring(s string) int {
	if len(s) == 0 {
		return 0
	}
	max := 0
	cMap := make(map[byte]int)
	s1 := 0

	for s2 := 0; s2 < len(s); s2++ {
		if s, ok := cMap[s[s2]]; ok && s >= s1 {
			s1 = s + 1
		}
		cMap[s[s2]] = s2

		if s2-s1+1 > max {
			max = s2 - s1 + 1
		}
	}
	return max
}

/**
 * Definition for singly-linked list.
 * type ListNode struct {
 *     Val int
 *     Next *ListNode
 * }
 */
type ListNode struct {
	Val  int
	Next *ListNode
}

func mergeTwoLists(list1 *ListNode, list2 *ListNode) *ListNode {
	if list1 == nil && list2 == nil {
		return nil
	}
	one := 0
	h1 := list1
	if list1 != nil {
		one = list1.Val
		list1 = list1.Next
	} else {
		return list2
	}
	if list2 != nil {
		if list2.Val < one {
			one = list2.Val
			list2 = list2.Next
			list1 = h1
		}
	} else {
		return h1
	}

	h := &ListNode{Val: one}
	cur := h
	for list1 != nil && list2 != nil {
		if list1.Val < list2.Val {
			cur.Next = list1
			list1 = list1.Next
		} else {
			cur.Next = list2
			list2 = list2.Next
		}
		cur = cur.Next
	}
	if list1 != nil {
		cur.Next = list1
	} else {
		cur.Next = list2
	}
	return h
}

func reverseList(head *ListNode) *ListNode {
	if head == nil {
		return nil
	}
	h := &ListNode{Val: -9999}
	for head != nil {
		cur := &ListNode{Val: head.Val}
		if h.Val != -9999 {
			cur.Next = h
		}
		h = cur
		head = head.Next
	}
	return h
}
func hasCycle(head *ListNode) bool {
	fast := head
	slow := head
	for fast != nil && fast.Next != nil {
		fast = fast.Next.Next
		slow = slow.Next
		if fast == slow {
			return true
		}
	}
	return false
}

func addTwoNumbers(l1 *ListNode, l2 *ListNode) *ListNode {
	return addNum(l1, l2, 0)
}

func addNum(list1 *ListNode, list2 *ListNode, num int) *ListNode {
	if list1 == nil && list2 == nil {
		if num != 0 {
			return &ListNode{Val: num}
		} else {
			return nil
		}
	}
	if list1 != nil {
		num = num + list1.Val
		list1 = list1.Next
	}
	if list2 != nil {
		num = num + list2.Val
		list2 = list2.Next
	}
	return &ListNode{num % 10, addNum(list1, list2, num/10)}
}

func isValid(s string) bool {
	n := len(s)
	if n%2 == 1 {
		return false
	}
	pairs := map[byte]byte{
		')': '(',
		']': '[',
		'}': '{',
	}
	stack := []byte{}
	for i := 0; i < n; i++ {
		if pairs[s[i]] > 0 {
			if len(stack) == 0 || stack[len(stack)-1] != pairs[s[i]] {
				return false
			}
			stack = stack[:len(stack)-1]
		} else {
			stack = append(stack, s[i])
		}
	}
	return len(stack) == 0
}
